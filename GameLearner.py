import random
from operator import attrgetter

class Token:
    def __init__(self, type_string = "empty"):
        self.type_string = type_string
    def is_type(self, other):
        return self.type_string == other
    def change_type(self, typeS):
        self.type_string = typeS
    def is_empty(self):
        return self.type_string == "empty"
    def __str__(self):
        if self.type_string == "empty":
            return "-"
        return str(self.type_string)


class Board:
    def __init__(self, width, height):
        self.width = width
        self.height = height
        self.matrix = []
        self.moves_left = width * height #must generalize
        for i in range(width):
            self.matrix.append([Token() for x in range(height)])
    def get_token(self, x, y):
        return self.matrix[x][y]
    def change_token(self, token, x, y):
        self.matrix[x][y] = token
    def make_move(self, move, token):
        to_change = self.matrix[move.xPos][move.yPos]
        if to_change.is_empty():
            self.moves_left -= 1;
        self.matrix[move.xPos][move.yPos] = token
    def potential_moves(self):
        moves = []
        for x in range(self.width):
            for y in range(self.height):
                curr_token = self.get_token(x, y)
                if curr_token.is_empty():
                    moves.append(Move(x, y))
        return moves
    def show_board(self):
        lst = []
        for y in range(self.height):
            lst.append(str([str(self.get_token(x, y)) for x in range(self.width)]))
        for elem in lst:
            print(elem)
    def __repr__(self):
        return repr(self.matrix)

class Player:
    def __init__(self, name, token = None):
        self.name = name
        self.token = Token(token)
    def set_board(self, board):
        self.board = board
        self.win_num = board.width #change for generalization
        self.rows = [0 for x in range(board.height)]
        self.cols = [0 for x in range(board.width)]
        self.diags = [0, 0] #change for generalization
        self.won_game = False
    def make_move(self, move):
        self.board.make_move(move, self.token)
        xPos = move.xPos
        yPos = move.yPos
        self.cols[xPos] += 1
        if self.cols[xPos] >= self.win_num:
            self.won_game = True
        self.rows[yPos] += 1
        if self.rows[yPos] >= self.win_num:
            self.won_game = True
        if xPos == yPos:
            self.diags[0] += 1
            if self.diags[0] >= self.win_num:
                self.won_game = True
        if xPos + yPos == self.board.width - 1: #change for generalization
            self.diags[1] += 1
            if self.diags[1] >= self.win_num:
                self.won_game = True 
    def follow_move(self, move):
        return
    def learn_draw(self):
        return
    def learn_game(self):
        return


class Human(Player):
    def determine_move(self):
        print(self.name + "'s Turn!")
        xPos = eval(input("Column:")) - 1
        yPos = eval(input("Row:")) - 1
        if xPos >= self.board.width or yPos >= self.board.height:
            print("Request out of bounds")
            return self.determine_move()
        elif self.board.get_token(xPos, yPos).is_empty():
            return Move(xPos, yPos)
        else:
            print("Space is full")
            return self.determine_move()

class RandomBot(Player):
    def determine_move(self):
        xPos = random.randint(0, self.board.width - 1)
        yPos = random.randint(0, self.board.height - 1)
        if self.board.get_token(xPos, yPos).is_empty():
            print(self.name + "'s Turn!")
            #input("Press Enter")
            new_token = Token(self.token)
            return Move(xPos, yPos)
        else:
            return self.determine_move()

class Move:
    def __init__(self, xPos, yPos):
        self.xPos = xPos
        self.yPos = yPos
    def __eq__(self, other):
        return self.xPos == other.xPos and self.yPos == other.yPos
    def __hash__(self):
        return hash((self.xPos, self.yPos))
    def __str__(self):
        return "(" + str(self.xPos) + ", " + str(self.yPos) + ")"

class Game:
    def __init__(self, player1, player2, width = 3, height = 3):
        self.board = Board(width, height)
        self.player1 = player1
        self.player2 = player2
        self.player1.set_board(self.board)
        self.player2.set_board(self.board)
        self.over = False
        self.num_moves = 0
        self.move_limit = width * height
    def play(self):
        self.board.show_board()
        while not self.over:
            p1_move = self.player1.determine_move()
            self.player2.follow_move(p1_move)
            self.player1.make_move(p1_move)
            self.board.show_board()
            self.num_moves += 1
            if self.player1.won_game:
                self.over = True
                self.player1.learn_game()
                self.player2.learn_game()
                print("Game is over! " + self.player1.name + " has won!")
                return self.player1
            if self.num_moves == self.move_limit:
                self.over = True
                #self.player1.learn_draw()
                #self.player2.learn_draw()
                print("Game is over! It's a draw!")
                return None
            p2_move = self.player2.determine_move()
            self.player2.make_move(p2_move)
            self.player1.follow_move(p2_move)
            self.board.show_board()
            self.num_moves += 1
            if self.player2.won_game:
                self.over = True
                self.player2.learn_game()
                self.player1.learn_game()
                print("Game is over! " + self.player2.name + " has won!")
                return self.player2
            if self.num_moves == self.move_limit:
                self.over = True
                # self.player1.learn_draw()
                # self.player2.learn_draw()
                print("Game is over! It's a draw!")
                return None

class MoveTree:
    def __init__(self, move_num, move = None, parent = None):
        self.move_num = 0
        self.move = move
        self.parent = parent
        self.children = []
        self.priority = 0
    def is_root(self):
        return self.move is None
    def add_child(self, move):
        for child in self.children:
            if move == child.move:
                return child
        new_tree = MoveTree(self.move_num + 1, move, self)
        self.children.append(new_tree)
        return new_tree
    def is_child(self, move):
        for child in self.children:
            if move == child.move:
                return True
        return False
    def prioritize(self, maximize = True):
        if maximize:
            maxP = max(self.parent.children, key=attrgetter('priority'))
            self.priority = maxP.priority + 1
        else:
            minP = min(self.parent.children, key=attrgetter('priority'))
            self.priority = minP.priority - 1
    def change_priority(self, num):
        self.priority += num
    def set_priority(self, num):
        self.priority = num
    def fill_children(self, board): #Make more efficient
        if len(self.children) == 0:
            potential = [MoveTree(self.move_num + 1, m, self) for m in board.potential_moves()]
            self.children.extend(potential)
        else:
            potential = [MoveTree(self.move_num + 1, m, self) for m in board.potential_moves() if not self.is_child(m)]
            self.children.extend(potential)
    def copy(self):
        new_copy = MoveTree(self.move_num, self.move, self.parent)
        new_copy.set_priority(self.priority)
        new_copy.children = [x.copy() for x in self.children]
        return new_copy

class LearnBot(Player):
    def __init__(self, name, token = None):
        self.name = name
        self.token = Token(token)
        self.curr_tree = MoveTree(0)
        self.board_trees = {}
    def set_board(self, board):
        self.board = board
        self.win_num = board.width #change for generalization
        self.rows = [0 for x in range(board.height)]
        self.cols = [0 for x in range(board.width)]
        self.diags = [0, 0] #change for generalization
        self.won_game = False
        max_move = Move(board.width - 1, board.height - 1)
        tree_to_use = [self.board_trees[m] for m in self.board_trees if max_move == m]
        if len(tree_to_use) == 1:
            self.curr_tree = tree_to_use[0]
        else:
            self.curr_tree = MoveTree(0)
            self.board_trees[max_move] = self.curr_tree
    def determine_move(self):
        print(self.name + "'s Turn!")
        if len(self.curr_tree.children) < self.board.moves_left:
            self.curr_tree.fill_children(self.board)
        random.shuffle(self.curr_tree.children)
        next_tree = max(self.curr_tree.children, key=attrgetter('priority'))
        self.curr_tree = next_tree
        # input("Press Enter")
        return next_tree.move
    def follow_move(self, move):
        next_tree = self.curr_tree.add_child(move)
        self.curr_tree = next_tree
    def learn_draw(self):
        while not self.curr_tree.is_root():
            self.curr_tree.change_priority(1)
            self.curr_tree = self.curr_tree.parent
    def learn_game(self):
        maximize = True
        while not self.curr_tree.is_root():
            self.curr_tree.prioritize(maximize)
            if maximize:
                maximize = False
            else:
                maximize = True
            self.curr_tree = self.curr_tree.parent
    def create_teacher(self):
        teacher = TeachBot("Teacher", "T")
        teacher.set_moveTree(self.curr_tree.copy())
        return teacher

class TeachBot(LearnBot):
    def set_moveTree(self, tree):
        self.curr_tree = tree
    def learn_draw(self):
        return
    def learn_game(self):
        return

        
John = Human("John", "X")

SmartBot = LearnBot("SmartBot", "O")

RandoJoe = RandomBot("RandoJoe", "R")

def teach(learner, dim = 3):
    win_ratios = []
    first = 0
    rando = RandomBot("Rando", "R")
    for i in range(50):
        teacher = LearnBot("Teacher" + str(i), str(i))
        for k in range(1000):
            Game(teacher, rando).play()
        wins = 0
        for j in range(1000):
            result = Game(teacher, learner).play()
            if result is None or result == learner:
                wins += 1
        win_ratios.append(wins / 1000)
    # for i in range(50):
    #     if learner == Game(RandoJoe, learner, dim, dim).play():
    #         first += 1
    # win_ratios.append(first / 1000)
    # for k in range(50):
    #     wins = 0
    #     new_teacher = learner.create_teacher()
    #     for z in range(500):
    #         result = Game(RandoJoe, learner, dim, dim).play()
    #         if result is None or result == learner:
    #             wins += 1
    #     for j in range(500):
    #         result = Game(new_teacher, learner, dim, dim).play()
    #         if result is None or result == learner:
    #             wins += 1
    #     win_ratios.append(wins / 1000)
    return win_ratios




