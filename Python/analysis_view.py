import pyOSC3 as OSC
import matplotlib.pyplot as plt


class View:
    def __init__(self, model):
        self.model = model
        self.client = OSC.OSCClient()
        self.connect()

    def sonify(self, raw_pos, velocity, reach_target, energy):
        #send to supercollider
        data = (raw_pos, velocity, str(reach_target), energy)
        print(data)
        oscmsg = OSC.OSCMessage()
        oscmsg.setAddress("/notch")
        oscmsg.append(data)
        try:
            self.client.send(oscmsg)
        except Exception as e:
            print("Error connecting to SuperCollider. Is SuperCollider listening on port 57120?")

    def show_graph(self, frames, positions, velocities, reaches, energies):
        fig, ax = plt.subplots(2, 2)
        ax[0, 0].plot(frames, positions)
        ax[0, 0].set(xlabel='frame', ylabel='raw angle', title='Angle')

        ax[0, 1].plot(frames, velocities)
        ax[0, 1].set(xlabel='frame', ylabel='velocity', title='Velocities')

        ax[1, 0].plot(frames, reaches)
        ax[1, 0].set(xlabel='frame', ylabel='Angle > Target Value', title='Reaches')

        ax[1, 1].plot(frames, energies)
        ax[1, 1].set(xlabel='frame', ylabel='Energy Function', title='Energies')

        plt.show()

    def connect(self):
        try:
            self.client.connect(('127.0.0.1', 57120))
        except Exception as e:
            print("Error connecting to Supercollider. Is SuperCollider listening on port 57120?")
