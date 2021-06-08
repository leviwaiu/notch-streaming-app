from analysis_view import View
import math

class Model:
    def __init__(self, controller, dimension, target_val):
        self.controller = controller
        self.view = View(self)
        self.dimension = dimension
        self.target_val = target_val
        self.firstReach = True
        self.frame = 0
        self.frames = []
        self.energies = []
        self.velocities = []
        self.reaches = []
        self.positions = []

    def detect_reach_target(self, pos):
        if abs(pos) > self.target_val:
            if self.firstReach:
                self.firstReach = False
                return True
            else:
                return False
        else:
            self.firstReach = True
            return False

    def calculate_energy(self, cur_row, prev_row, filter_value):
        sum = 0
        vels = [0, 0, 0, 0]
        for i in range(len(cur_row)):
            velocity = (cur_row[i] - prev_row[i]) / 0.025
            if velocity > filter_value:
                velocity = filter_value
            elif velocity < -filter_value:
                velocity = -filter_value
            vels[i] = velocity
            sum += velocity ** 2
        return math.sqrt(sum), vels

    def graph_data(self):
        self.view.show_graph(self.frames, self.positions, self.velocities, self.reaches, self.energies)

    def update(self, current_data, prev_data):
        position = current_data[self.dimension]
        energy, velocities = self.calculate_energy(current_data, prev_data, 300)
        reached_target = self.detect_reach_target(position)

        self.energies.append(energy)
        self.velocities.append(velocities[self.dimension])
        self.positions.append(position)
        self.reaches.append(reached_target)
        self.frames.append(self.frame)
        self.frame += 1

        self.view.sonify(position, velocities[self.dimension], reached_target, energy)