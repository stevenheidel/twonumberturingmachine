sealed trait Alphabet
case object Zero extends Alphabet
case object One extends Alphabet

sealed trait Direction
case object Left extends Direction
case object Right extends Direction
case object Neither extends Direction

case class Command(
  toWrite: Alphabet,
  toMove: Direction,
  toState: Int
)

sealed trait AnyState
case class State(
  onZero: Command,
  onOne: Command
) extends AnyState
case object AcceptState extends AnyState

case class TuringMachine (
  // list of states, initial is first by default
  states: Seq[AnyState],
  // current state
  currentState: Int = 1,
  // all the tape left of the pointer
  left: Long = 0,
  // all the tape right of and including the pointer, then reversed
  right: Long = 0
) {
  def setInput(input: Seq[Alphabet]): TuringMachine = {
    if (input.length > TuringMachine.maxLength)
      throw new IllegalStateException("Tape bound exceeded by input")

    val inputInBinary: Long = input.foldRight(0L) {
      case (Zero, x) => (x << 1)
      case (One, x) => (x << 1) + 1
    }

    copy(right = inputInBinary)
  }

  def step: Option[TuringMachine] = {
    // states use 1-based indexing
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
      if (right > TuringMachine.maxVal)
        throw new IllegalStateException("Tape bound exceeded on right side")

      copy(
        left = (left >> 1),
        right = (right << 1) + (left & 1)
      )
    case Right =>
      if (left > TuringMachine.maxVal)
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
  val maxVal = (1L << maxLength - 1) - 1

  def run(machine: TuringMachine): Stream[TuringMachine] = {
    // unfold function from Haskell on one type and to a stream instead
    def unfoldRight[A](z: A)(f: A => Option[A]): Stream[A] = z #:: (f(z) match {
      case Some(a) => unfoldRight(a)(f)
      case None => Stream.empty
    })

    unfoldRight(machine)(_.step)
  }
}

object Main extends App {
  // a 3-state, 2-symbol busy beaver
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

  val outputFormat = s"%s\n%-${TuringMachine.maxLength}d^%${TuringMachine.maxLength - 1}d\n\n"

  TuringMachine.run(busyBeaver).toList.map { step =>
    printf(outputFormat, step.getTape, step.left, step.right)
  }
}

Main.main(Array.empty)
