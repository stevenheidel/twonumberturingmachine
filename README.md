# Two Number Turing Machine
A turing machine whose tape is implemented with just two numbers.

From Wikipedia [[1]](https://en.wikipedia.org/wiki/Counter_machine):

A Turing machine consists of an FSM and an infinite tape, initially filled with zeros, upon which the machine can write ones and zeros. At any time, the read/write head of the machine points to one cell on the tape. This tape can be conceptually cut in half at that point. Each half of the tape can be treated as a stack, where the top is the cell nearest the read/write head, and the bottom is some distance away from the head, with all zeros on the tape beyond the bottom. Accordingly, a Turing machine can be simulated by an FSM plus two stacks. Moving the head left or right is equivalent to popping a bit from one stack and pushing it onto the other. Writing is equivalent to changing the bit before pushing it.

A stack containing zeros and ones can be simulated by a counter when the bits on the stack are thought of as representing a binary number (the topmost bit on the stack being the least significant bit). Pushing a zero onto the stack is equivalent to doubling the number. Pushing a one is equivalent to doubling and adding 1. Popping is equivalent to dividing by 2, where the remainder is the bit that was popped.
