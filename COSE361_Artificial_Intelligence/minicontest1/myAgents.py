# myAgents.py
# ---------------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
#
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).

from game import Agent
from searchProblems import PositionSearchProblem

import util
import time
import search

"""
IMPORTANT
`agent` defines which agent you will use. By default, it is set to ClosestDotAgent,
but when you're ready to test your own agent, replace it with MyAgent
"""
def createAgents(num_pacmen, agent='myAgent'):
    return [eval(agent)(index=i) for i in range(num_pacmen)]

class MyAgent(Agent):
    """
    Implementation of your agent.
    """
    dist = 0
    stopped = set()

    def getAction(self, state):
        """
        Returns the next action the agent will take
        """

        food = state.getFood()
        problem = AnyFoodSearchProblem(state, self.index)

        if self.index == 0:
            MyAgent.dist = [[9999 for _ in range(food.height)] for _ in range(food.width)]
        if self.index in MyAgent.stopped:
            return 'Stop'

        pacmanCurrent = [problem.getStartState(), []]
        visitedPosition = set()
        visitedPosition.add(pacmanCurrent[0])
        fringe = util.Queue()
        fringe.push(pacmanCurrent)
        distCurrent = self.index

        while not fringe.isEmpty():
            fringeSize = len(fringe.list)
            for _ in range(fringeSize):
                pacmanCurrent = fringe.pop()
                pacmanPos = pacmanCurrent[0]
                if problem.isGoalState(pacmanPos) and distCurrent < MyAgent.dist[pacmanPos[0]][pacmanPos[1]]:

                    MyAgent.dist[pacmanPos[0]][pacmanPos[1]] = distCurrent
                    subFringe = util.Queue()
                    searchRange = 7
                    successors = problem.getSuccessors(pacmanPos)
                    for item in successors:
                        if item[0] not in visitedPosition:
                            visitedPosition.add(item[0])
                            subFringe.push(item[0])

                    while not subFringe.isEmpty() and searchRange > 0:
                        subFringeSize = len(subFringe.list)
                        for _ in range(subFringeSize):
                            subPacmanCurrent = subFringe.pop()
                            if problem.isGoalState(subPacmanCurrent) and distCurrent < MyAgent.dist[subPacmanCurrent[0]][subPacmanCurrent[1]]:
                                MyAgent.dist[subPacmanCurrent[0]][subPacmanCurrent[1]] = distCurrent
                            successors = problem.getSuccessors(subPacmanCurrent)
                            for item in successors:
                                if item[0] not in visitedPosition:
                                    visitedPosition.add(item[0])
                                    subFringe.push(item[0])

                        distCurrent += 1
                        searchRange -= 1

                    return pacmanCurrent[1][0]
                else:
                    pacmanSuccessors = problem.getSuccessors(pacmanCurrent[0])
                Successor = []
                for item in pacmanSuccessors:  # item: [(x,y), 'direction', cost]
                    if item[0] not in visitedPosition:
                        visitedPosition.add(pacmanPos)
                        pacmanRoute = pacmanCurrent[1].copy()
                        pacmanRoute.append(item[1])
                        Successor.append([item[0], pacmanRoute])
                for item in Successor:
                    fringe.push(item)

            distCurrent += 1

        MyAgent.stopped.add(self.index)
        return 'Stop'



    def initialize(self):
        """
        Intialize anything you want to here. This function is called
        when the agent is first created. If you don't need to use it, then
        leave it blank
        """

        # raise NotImplementedError()

"""
Put any other SearchProblems or search methods below. You may also import classes/methods in
search.py and searchProblems.py. (ClosestDotAgent as an example below)
"""

class ClosestDotAgent(Agent):

    def findPathToClosestDot(self, gameState):
        """
        Returns a path (a list of actions) to the closest dot, starting from
        gameState.
        """
        # Here are some useful elements of the startState
        startPosition = gameState.getPacmanPosition(self.index)
        food = gameState.getFood()
        walls = gameState.getWalls()
        problem = AnyFoodSearchProblem(gameState, self.index)


        "*** YOUR CODE HERE ***"

        pacmanCurrent = [problem.getStartState(), [], 0]
        visitedPosition = set()
        # visitedPosition.add(problem.getStartState())
        fringe = util.PriorityQueue()
        fringe.push(pacmanCurrent, pacmanCurrent[2])
        while not fringe.isEmpty():
            pacmanCurrent = fringe.pop()
            if pacmanCurrent[0] in visitedPosition:
                continue
            else:
                visitedPosition.add(pacmanCurrent[0])
            if problem.isGoalState(pacmanCurrent[0]):
                return pacmanCurrent[1]
            else:
                pacmanSuccessors = problem.getSuccessors(pacmanCurrent[0])
            Successor = []
            for item in pacmanSuccessors:  # item: [(x,y), 'direction', cost]
                if item[0] not in visitedPosition:
                    pacmanRoute = pacmanCurrent[1].copy()
                    pacmanRoute.append(item[1])
                    sumCost = pacmanCurrent[2]
                    Successor.append([item[0], pacmanRoute, sumCost + item[2]])
            for item in Successor:
                fringe.push(item, item[2])
        return pacmanCurrent[1]

    def getAction(self, state):
        return self.findPathToClosestDot(state)[0]

class AnyFoodSearchProblem(PositionSearchProblem):
    """
    A search problem for finding a path to any food.

    This search problem is just like the PositionSearchProblem, but has a
    different goal test, which you need to fill in below.  The state space and
    successor function do not need to be changed.

    The class definition above, AnyFoodSearchProblem(PositionSearchProblem),
    inherits the methods of the PositionSearchProblem.

    You can use this search problem to help you fill in the findPathToClosestDot
    method.
    """

    def __init__(self, gameState, agentIndex):
        "Stores information from the gameState.  You don't need to change this."
        # Store the food for later reference
        self.food = gameState.getFood()

        # Store info for the PositionSearchProblem (no need to change this)
        self.walls = gameState.getWalls()
        self.startState = gameState.getPacmanPosition(agentIndex)
        self.costFn = lambda x: 1
        self._visited, self._visitedlist, self._expanded = {}, [], 0 # DO NOT CHANGE

    def isGoalState(self, state):
        """
        The state is Pacman's position. Fill this in with a goal test that will
        complete the problem definition.
        """
        x,y = state
        if self.food[x][y] == True:
            return True
        return False

