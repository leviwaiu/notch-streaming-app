import sys
from analysis_controller import Controller

if __name__ == '__main__':
    test_analysis = Controller(sys.argv, True)
    test_analysis.run()