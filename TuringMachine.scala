sealed trait Alphabet
case object Zero extends Alphabet
case object One extends Alphabet

sealed trait Direction
case object Left extends Direction
case object Right extends Direction
case object Neither extends Direction

case class Command(
  val toWrite: Alphabet,
  val toMove: Direction,
  val toState: Int
)

sealed trait AnyState
case class State(
  val onZero: Command,
  val onOne: Command
) extends AnyState
case object AcceptState extends AnyState

case class TuringMachine(
  // list of states, initial is first by default
  states: Seq[AnyState],
  // current state
  private val currentState: Int = 1,
  // all the tape left of the pointer
  private val left: Long = 0,
  // all the tape right of and including the pointer, then reversed
  private val right: Long = 0
) {
  def setInput(input: Seq[Alphabet]): TuringMachine = {
    val inputInBinary: Long = input.foldRight(0L) { (i, sum) => i match {
      case Zero => (sum << 1)
      case One => (sum << 1) + 1
    }}

    copy(right = inputInBinary)
  }

  // states use 1-based indexing
  def step: Option[TuringMachine] = {
    if (!states.isDefinedAt(currentState - 1))
      throw new IllegalStateException(s"Literally, an illegal state: $currentState")

    states(currentState - 1) match {
      case State(onZero, onOne) => read match {
        case Zero => Some(runCommand(onZero))
        case One => Some(runCommand(onOne))
      }
      case AcceptState => None
    }
  }

  def getTape: String = {
    def toBinary(x: Long) =
      String.format("%" + TuringMachine.maxLength + "s", x.toBinaryString).replace(' ', '0')

    toBinary(left) + toBinary(right).reverse
  }

  private def runCommand(command: Command): TuringMachine = {
    write(command.toWrite).move(command.toMove).changeState(command.toState)
  }

  private def read: Alphabet = {
    if ((right & 1) == 0) Zero else One
  }

  private def write(symbol: Alphabet): TuringMachine = symbol match {
    case Zero => copy(right = (right & ~1))
    case One => copy(right = (right | 1))
  }

  // this is the direction to move the head, not to move the tape
  private def move(direction: Direction): TuringMachine = direction match {
    case Left =>
      if (right >= TuringMachine.maxVal)
        throw new IllegalStateException("Tape bound exceeded on right side")

      copy(
        left = (left >> 1),
        right = (right << 1) + (left & 1)
      )
    case Right =>
      if (left >= TuringMachine.maxVal)
        throw new IllegalStateException("Tape bound exceeded on left side")

      copy(
        left = (left << 1) + (right & 1),
        right = (right >> 1)
      )
    case Neither => copy()
  }

  private def changeState(newState: Int): TuringMachine = {
    copy(currentState = newState)
  }
}

object TuringMachine {
  // max distance from starting point that a 1 can be written
  val maxLength = 40
  val maxVal = 1L << maxLength - 1

  def run(machine: TuringMachine): Stream[TuringMachine] = {
    // Unfold function from Haskell on one type and to a stream instead
    def unfoldRight[A](z: A)(f: A => Option[A]): Stream[A] = z #:: (f(z) match {
      case Some(a) => unfoldRight(a)(f)
      case None => Stream.empty
    })

    unfoldRight(machine)(_.step)
  }
}

object Main extends App {
  // A 3-state, 2-symbol busy beaver
  val busyBeaverStates = Seq(
    State(
      Command(One, Right, 2),
      Command(One, Left, 3)
    ),
    State(
      Command(One, Left, 1),
      Command(One, Right, 2)
    ),
    State(
      Command(One, Left, 2),
      Command(One, Neither, 4)
    ),
    AcceptState
  )

  val busyBeaver = TuringMachine(busyBeaverStates).setInput(Seq(Zero))

  TuringMachine.run(busyBeaver).toList.map { step =>
    println(step.getTape)
    println(" " * TuringMachine.maxLength + "^")
    println()
  }
}

Main.main(Array.empty)
