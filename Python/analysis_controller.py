import os
import csv
import time
import ipaddress
import datetime
import pandas as pd
from oscpy.server import OSCThreadServer
from time import sleep
from analysis_model import Model

class Controller:
    def __init__(self, input_args, test_mode):
        self.test_mode = test_mode
        self.ipaddress = "127.0.0.1"
        self.dim = 0
        self.target_val = 100
        self.read_dir = ""
        self.previous_row = [0, 0, 0, 0]
        self.receive_port = 7771

        self.check_input_args(input_args)
        self.model = Model(self, self.dim, self.target_val)

    def check_input_args(self, args):
        if not self.test_mode:
            try:
                self.ipaddress = str(args[1])
                #check if its a valid IP address
                ip = ipaddress.ip_address(args[1])
            except IndexError as ie:
                print("Missing IP address argument. analysis_realtime_mode.py should be run with three input arguments: "
                      "\n 1) IP address for recieving OSC messages \n 2) bend/twist/lean/reach \n 3) target threshold value")
                raise ie
            except ValueError as ve:
                print("Please enter a valid IP address as the first argument")
                raise ve
        else:
            try:
                self.read_dir = str(args[1])
                assert os.path.isdir(self.read_dir)
            except IndexError as ie:
                print("Missing directory argument. analysis_test_mode.py should be run with three input arguments: "
                      "\n 1) read directory for data \n 2) bend/twist/lean/reach \n 3) target threshold value")
                raise ie
            except AssertionError as ae:
                print("Invalid directory argument - please enter a valid directory as an input argument")
                raise ae

        try:
            dimension = str(args[2])
            assert dimension == "bend" or dimension == "reach" or dimension == "lean" or dimension == "twist"
        except IndexError as ie:
            print("Missing dimension argument. Analysis module should be run with three input arguments: "
                  "\n 1) read directory for data OR IP address \n 2) bend/twist/lean/reach \n 3) target threshold value")
            raise ie
        except AssertionError as ae:
            print("Invalid dimension argument - should be bend OR reach OR twist OR lean")
            raise ae
        if dimension == "lean":
            self.dim = 0
        elif dimension == "twist":
            self.dim = 1
        elif dimension == "reach":
            self.dim = 2
        else:
            self.dim = 3
        try:
            self.target_val = int(args[3])
            assert -1000 < self.target_val < 1000
        except IndexError as ie:
            print("Missing target threshold value argument. Analysis module should be run with three "
                  "input arguments: "
                  "\n 1) read directory for data OR IP addres \n 2) bend/twist/lean/reach \n 3) target threshold value")
            raise ie
        except ValueError as te:
            print("Please enter an integer as the target value.")
            raise te
        except AssertionError as ae:
            print("Target value argument should be an integer between -1000 and 1000. You can use the notch app "
                  "to calculate the target value")
            raise ae


    def merge_csv_data(self, path):
        dt = datetime.datetime.now()
        date_string = dt.strftime('%m%d%Y')
        merged_filename = "merged_data" + date_string + ".csv"
        try:
            chest_angles = pd.read_csv(path + "/Angles_Chest.csv",
                                       usecols=['Anterior(+)/posterior(-) tilt', 'Rotation left(+)/right(-)'])
        except FileNotFoundError as fnfe:
            print("No chest angles file found. Please make sure Angles_Chest.csv is present in read directory")
            raise fnfe
        except ValueError as ve:
            print("Columns are missing from Angles_Chest.csv. Make sure "
                  "'Anterior(+)/posterior(-) tilt', 'Rotation left(+)/right(-)' columns are present")
            raise ve

        try:
            shoulder_angles = pd.read_csv(path + "/Angles_RightShoulder.csv", usecols=['Flexion(-)/extension(+)'])
        except FileNotFoundError as fnfe:
            print("No shoulder angles file found. Please make sure Angles_RightShoulder.csv is present in read directory")
            raise fnfe
        except ValueError as ve:
            print("Columns are missing from Angles_RightShoulder.csv. Make sure "
                  "'Flexion(-)/extension(+)' column is present")
            raise ve

        try:
            knee_angles = pd.read_csv(path + "/Angles_RightKnee.csv", usecols=['Flexion(+)/extension(-)'])
        except FileNotFoundError as fnfe:
            print("No knee angles file found. Please make sure Angles_RightKnee.csv is present in read directory")
            raise fnfe
        except ValueError as ve:
            print("Columns are missing from Angles_RightKnee.csv. Make sure "
                  "'Flexion(+)/extension(-)' column is present")
            raise ve

        try:
            merged = pd.concat([chest_angles, shoulder_angles, knee_angles], axis=1)
            merged.to_csv(merged_filename)
        except Exception as e:
            print("Error merging measurement data. Check dimensions of csv files match")
            raise e
        return merged_filename

    def read_csv(self, filename):
        with open(filename) as csv_file:
            csv_reader = csv.reader(csv_file, delimiter=',')
            next(csv_reader, None)
            for row in csv_reader:
                print(row)
                current_row = [float(row[1]), float(row[2]), float(row[3]), float(row[4])]
                self.model.update(current_row, self.previous_row)
                self.previous_row = current_row
                time.sleep(0.021)

    def receive_osc_callback(self, msg1, msg2, msg3, msg4):
        # incoming values = [chest_angle, chest_rotation, right_shoulder_angle, right_knee_angle]
        try:
            current_row = [float(msg1.decode('utf-8')), float(msg2.decode('utf-8')),
                           float(msg3.decode('utf-8')), float(msg4.decode('utf-8'))]
        except ValueError as ve:
            print("Error when parsing incoming OSC messages. Incoming msgs from Android app should consist of four "
                  "floats which have been utf-8 encoded as strings before sending.")
            raise ve
        except AttributeError as ae:
            print("Error when parsing incoming OSC messages. Check behaviour of Notch app (is it sending NaN values?")
        self.model.update(current_row, self.previous_row)
        self.previous_row = current_row

    def run(self):
        if self.test_mode:
            merged_filename = self.merge_csv_data(self.read_dir)
            self.read_csv(merged_filename)
            self.model.graph_data()
        else:
            osc = OSCThreadServer()
            try:
                sock = osc.listen(address=self.ipaddress, port=self.receive_port, default=True)
                byte_ip_address = ('/' + self.ipaddress).encode('UTF-8')
                osc.bind(byte_ip_address, self.receive_osc_callback)
            except Exception as e:
                print("Error when attempting to open incoming osc port. Check provided IP address is correct"
                      "and that port 7771 is not already in use")
                raise e
            sleep(1000)
            osc.stop()





