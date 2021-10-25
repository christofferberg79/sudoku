# Valid Test Cases
These test cases should result in a valid puzzle, and potentially a unique solution.

Contents
1. Completed Puzzle
2. Last Empty Square
3. Naked Singles
4. Hidden Singles

## Completed Puzzle
A case of ‘more than enough’, this puzzle has already been solved. Nonetheless, it is still a valid puzzle, and should be parsed successfully.

Givens
```
9 7 4 | 2 3 6 | 1 5 8
6 3 8 | 5 9 1 | 7 4 2
1 2 5 | 4 8 7 | 9 3 6
------+-------+------
3 1 6 | 7 5 4 | 2 8 9
7 4 2 | 9 1 8 | 5 6 3
5 8 9 | 3 6 2 | 4 1 7
------+-------+------
8 6 7 | 1 2 5 | 3 9 4
2 5 3 | 6 4 9 | 8 7 1
4 9 1 | 8 7 3 | 6 2 5
```
`974236158638591742125487936316754289742918563589362417867125394253649871491873625`

Solution\
`974236158638591742125487936316754289742918563589362417867125394253649871491873625`

Pass Condition\
Unique Solution Found

## Last Empty Square
This puzzle requires only a single value to be placed on the grid in order to reach the unique solution: a ‘3’ in square r5c5.

Givens
```
2 5 6 | 4 8 9 | 1 7 3
3 7 4 | 6 1 5 | 9 8 2
9 8 1 | 7 2 3 | 4 5 6
------+-------+------
5 9 3 | 2 7 4 | 8 6 1
7 1 2 | 8 . 6 | 5 4 9
4 6 8 | 5 9 1 | 3 2 7
------+-------+------
6 3 5 | 1 4 7 | 2 9 8
1 2 7 | 9 5 8 | 6 3 4
8 4 9 | 3 6 2 | 7 1 5
```
`2564891733746159829817234565932748617128.6549468591327635147298127958634849362715`

Solution\
`256489173374615982981723456593274861712836549468591327635147298127958634849362715`

Pass Condition\
Unique Solution Found

## Naked Singles
This puzzle can be solved using only ‘Naked Singles’—also called ‘sole candidates’—which occur when a square has only one possible value remaining. The most obvious example of a Naked Single is the last square in any box, column, or row.

Givens
```
3 . 5 | 4 2 . | 8 1 .
4 8 7 | 9 . 1 | 5 . 6
. 2 9 | . 5 6 | 3 7 4
------+-------+------
8 5 . | 7 9 3 | . 4 1
6 1 3 | 2 . 8 | 9 5 7
. 7 4 | . 6 5 | 2 8 .
------+-------+------
2 4 1 | 3 . 9 | . 6 5
5 . 8 | 6 7 . | 1 9 2
. 9 6 | 5 1 2 | 4 . 8
```
`3.542.81.4879.15.6.29.5637485.793.416132.8957.74.6528.2413.9.655.867.192.965124.8`

Solution\
`365427819487931526129856374852793641613248957974165283241389765538674192796512438`

Pass Condition\
Unique Solution

Naked Singles:
```
r1c2 => 6	r1c6 => 7	r1c9 => 9	r2c5 => 3
r2c8 => 2	r3c1 => 1	r3c4 => 8	r4c3 => 2
r4c7 => 6	r5c5 => 4	r6c1 => 9	r6c4 => 1
r6c9 => 3	r7c5 => 8	r7c7 => 7	r8c2 => 3
r8c6 => 4	r9c1 => 7	r9c8 => 3
```

## Hidden Singles
This puzzle requires ‘Hidden Singles’ to solve, which occur when a box, column, or row has only one possible square remaining for a given value.

Givens
```
. . 2 | . 3 . | . . 8
. . . | . . 8 | . . .
. 3 1 | . 2 . | . . .
------+-------+------
. 6 . | . 5 . | 2 7 .
. 1 . | . . . | . 5 .
2 . 4 | . 6 . | . 3 1
------+-------+------
. . . | . 8 . | 6 . 5
. . . | . . . | . 1 3
. . 5 | 3 1 . | 4 . .
```
`..2.3...8.....8....31.2.....6..5.27..1.....5.2.4.6..31....8.6.5.......13..531.4..`

Solution\
`672435198549178362831629547368951274917243856254867931193784625486592713725316489`

Pass Condition\
Unique Solution

Hidden Singles:
```
r3c1 => 8	r7c1 => 1	r2c7 => 3	r2c4 => 1
r1c7 => 1	r3c7 => 5	r6c2 => 5	r2c1 => 5
r4c6 => 1	r5c6 => 3	r4c1 => 3	r7c3 => 3
r5c4 => 2	r5c9 => 6	r5c5 => 4	r4c9 => 4
r8c7 => 7
```

# Invalid Test Cases
Testing a good Sudoku solver should exercise the error handling as well as the algorithm. These test cases can be divided into to basic categories: puzzles that cannot be parsed, and puzzles that cannot be solved. A puzzle cannot be parsed if it violates any of the basic rules of Sudoku, or contains less than 17 given (starting) values. Alternately, many puzzles can be parsed successfully, but still cannot be solved: either the starting givens don’t yield a single unique solution, or there is no possible solution.

Contents
1. Empty
2. Single Given
3. Insufficient Givens
4. Duplicate Given — Box
5. Duplicate Given — Column
6. Duplicate Given — Row
7. Unsolvable Square
8. Unsolvable Box
9. Unsolvable Column
10. Unsolvable Row
11. Not Unique — 2 Solutions
12. Not Unique — 3 Solutions
13. Not Unique — 4 Solutions
14. Not Unique — 10 Solutions
15. Not Unique — 125 Solutions

## Empty
The ultimate in ‘not enough input’, this puzzle contains no givens at all. To say that it has multiple solutions is something of an overstatement.

Givens
```
. . . | . . . | . . .
. . . | . . . | . . .
. . . | . . . | . . .
------+-------+------
. . . | . . . | . . .
. . . | . . . | . . .
. . . | . . . | . . .
------+-------+------
. . . | . . . | . . .
. . . | . . . | . . .
. . . | . . . | . . .
```
`.................................................................................`

Solution\
(multiple solutions)

Pass Condition\
Invalid Puzzle (“not enough givens” / “multiple solutions”)

## Single Given
Originally described as ‘Zen Sudoku’, rumor has it that the only way to solve this puzzle is to stare at the lone ‘1’ in the center of the grid. Expected results could include “not enough starting values” or “multiple solutions”.

Givens
```
. . . | . . . | . . .
. . . | . . . | . . .
. . . | . . . | . . .
------+-------+------
. . . | . . . | . . .
. . . | . 1 . | . . .
. . . | . . . | . . .
------+-------+------
. . . | . . . | . . .
. . . | . . . | . . .
. . . | . . . | . . .
```
`........................................1........................................`

Solution\
(500+ solutions)

Pass Condition\
Invalid Puzzle (“not enough givens” / “multiple solutions”)

## Insufficient Givens
This puzzle has only sixteen givens, which is one less than the accepted minimum number for a classic Sudoku puzzle. Expected results could include “not enough starting values” or “multiple solutions”.

Givens
```
. . . | . . . | . . .
. . 5 | . . . | . 9 .
. . 4 | . . . | . 1 .
------+-------+------
2 . . | . . 3 | . 5 .
. . . | 7 . . | . . .
4 3 8 | . . . | 2 . .
------+-------+------
. . . | . 9 . | . . .
. 1 . | 4 . . | . 6 .
. . . | . . . | . . .
```
`...........5....9...4....1.2....3.5....7.....438...2......9.....1.4...6..........`

Solution\
(500+ solutions)

Pass Condition\
Invalid Puzzle (“not enough givens” / “multiple solutions”)

## Duplicate Given — Box
This puzzle cannot be solved, because the middle box (b5) has the value ‘5’ twice.

Givens
```
. . 9 | . 7 . | . . 5
. . 2 | 1 . . | 9 . .
1 . . | . 2 8 | . . .
------+-------+------
. 7 . | . . 5 | . . 1
. . 8 | 5 1 . | . . .
. 5 . | . . . | 3 . .
------+-------+------
. . . | . . 3 | . . 6
8 . . | . . . | . . .
2 1 . | . . . | . 8 7
```
`..9.7...5..21..9..1...28....7...5..1..851.....5....3.......3..68........21.....87`

Solution\
(no solution)

Pass Condition\
Invalid Puzzle (“no solution”)

## Duplicate Given — Column
This puzzle cannot be solved, because the middle column (c5) has the value ‘1’ twice.

Givens
```
6 . 1 | 5 9 . | . . .
. 9 . | . 1 . | . . .
. . . | . . . | . . 4
------+-------+------
. 7 . | 3 1 4 | . . 6
. 2 4 | . . . | . . 5
. . 3 | . . . | . 1 .
------+-------+------
. . 6 | . . . | . . 3
. . . | 9 . 2 | . 4 .
. . . | . . 1 | 6 . .
```
`6.159.....9..1............4.7.314..6.24.....5..3....1...6.....3...9.2.4......16..`

Solution\
(no solution)

Pass Condition\
Invalid Puzzle (“no solution”)

## Duplicate Given — Row
This puzzle cannot be solved, because the middle row (r5) has the value ‘2’ twice.

Givens
```
. 4 . | 1 . . | 3 5 .
. . . | . . . | . . .
. . . | 2 . 5 | . . .
------+-------+------
. . . | 4 . 8 | 9 . .
2 6 . | . . . | . 1 2
. 5 . | 3 . . | . . 7
------+-------+------
. . 4 | . . . | 1 6 .
6 . . | . . 7 | . . .
. 1 . | . 8 . | . 2 .
```
`.4.1..35.............2.5......4.89..26.....12.5.3....7..4...16.6....7....1..8..2.`

Solution\
(no solution)

Pass Condition\
Invalid Puzzle (“no solution”)

## Unsolvable Square
This puzzle cannot be solved, because the left-most square of the middle row (r5c1) has no possible candidates.

Givens
```
. . 9 | . 2 8 | 7 . .
8 . 6 | . . 4 | . . 5
. . 3 | . . . | . . 4
------+-------+------
6 . . | . . . | . . .
. 2 . | 7 1 3 | 4 5 .
. . . | . . . | . . 2
------+-------+------
3 . . | . . . | 5 . .
9 . . | 4 . . | 8 . 7
. . 1 | 2 5 . | 3 . .
```
`..9.287..8.6..4..5..3.....46.........2.71345.........23.....5..9..4..8.7..125.3..`

Solution\
(no solution)

Pass Condition\
Invalid Puzzle (“no solution”)

## Unsolvable Box
This puzzle cannot be solved, because the center box (b5) has no possible candidates for the value ‘4’.

Givens
```
. 9 . | 3 . . | . . 1
. . . | . 8 . | . 4 6
. . . | . . . | 8 . .
------+-------+------
4 . 5 | . 6 . | . 3 .
. . 3 | 2 7 5 | 6 . .
. 6 . | . 1 . | 9 . 4
------+-------+------
. . 1 | . . . | . . .
5 8 . | . 2 . | . . .
2 . . | . . 7 | . 6 .
```
`.9.3....1....8..46......8..4.5.6..3...32756...6..1.9.4..1......58..2....2....7.6.`

Solution\
(no solution)

Pass Condition\
Invalid Puzzle (“no solution”)

## Unsolvable Column
This puzzle cannot be solved, because the middle column (c5) has no possible candidates for the value ‘2’.

Givens
```
. . . | . 4 1 | . . .
. 6 . | . . . | . 2 .
. . 2 | . . . | . . .
------+-------+------
3 2 . | 6 . . | . . .
. . . | . 5 . | . 4 1
7 . . | . . . | . . 2
------+-------+------
. . . | . . . | 2 3 .
. 4 8 | . . . | . . .
5 . 1 | . . 2 | . . .
```
`....41....6.....2...2......32.6.........5..417.......2......23..48......5.1..2...`

Solution\
(no solution)

Pass Condition\
Invalid Puzzle (“no solution”)

## Unsolvable Row
This puzzle cannot be solved, because the middle row (r5) has no possible candidates for the value ‘1’.

Givens
```
9 . . | 1 . . | . . 4
. 1 4 | . 3 . | 8 . .
. . 3 | . . . | . 9 .
------+-------+------
. . . | 7 . 8 | . . 1
8 . . | . . 3 | . . .
. . . | . . . | . 3 .
------+-------+------
. 2 1 | . . . | . 7 .
. . 9 | . 4 . | 5 . .
5 . . | . 1 6 | . . 3
```
`9..1....4.14.3.8....3....9....7.8..18....3..........3..21....7...9.4.5..5...16..3`

Solution\
(no solution)

Pass Condition\
Invalid Puzzle (“no solution”)

## Not Unique — 2 Solutions
This puzzle is not a valid Sudoku, because it has two possible solutions.

Givens
```
. 3 9 | . . . | 1 2 .
. . . | 9 . 7 | . . .
8 . . | 4 . 1 | . . 6
------+-------+------
. 4 2 | . . . | 7 9 .
. . . | . . . | . . .
. 9 1 | . . . | 5 4 .
------+-------+------
5 . . | 1 . 9 | . . 3
. . . | 8 . 5 | . . .
. 1 4 | . . . | 8 7 .
```
`.39...12....9.7...8..4.1..6.42...79...........91...54.5..1.9..3...8.5....14...87.`

Solution\
`439658127156927384827431956342516798785294631691783542578149263263875419914362875`\
`439658127156927384827431956642513798785294631391786542578149263263875419914362875`

Pass Condition\
Invalid Puzzle (“no unique solution”)

## Not Unique — 3 Solutions
This puzzle is not a valid Sudoku, because it has three possible solutions.

Givens
```
. . 3 | . . . | . . 6
. . . | 9 8 . | . 2 .
9 4 2 | 6 . . | 7 . .
------+-------+------
4 5 . | . . 6 | . . .
. . . | . . . | . . .
1 . 9 | . 5 . | 4 7 .
------+-------+------
. . . | . 2 5 | . 4 .
6 . . | . 7 8 | 5 . .
. . . | . . . | . . .
```
`..3.....6...98..2.9426..7..45...6............1.9.5.47.....25.4.6...785...........`

Solution\
`783542196516987324942631758457296813238714965169853472891325647624178539375469281`\
`783542916516987324942631758457216839238794165169853472891325647624178593375469281`\
`783542916516987324942631758457216893238794165169853472891325647624178539375469281`

Pass Condition\
Invalid Puzzle (“no unique solution”)

## Not Unique — 4 Solutions
This puzzle is not a valid Sudoku, because it has four possible solutions.

Givens
```
. . . | . 9 . | . . .
6 . . | 4 . 7 | . . 8
. 4 . | 8 1 2 | . 3 .
------+-------+------
7 . . | . . . | . . 5
. . 4 | . . . | 9 . .
5 . . | 3 7 1 | . . 4
------+-------+------
. 5 . | . 6 . | . 4 .
2 . 1 | 7 . 8 | 5 . 9
. . . | . . . | . . .
```
`....9....6..4.7..8.4.812.3.7.......5..4...9..5..371..4.5..6..4.2.17.85.9.........`

Solution\
`178693452623457198945812736716984325384526917592371684857169243231748569469235871`\
`178693452623457198945812736716984325384526971592371684857169243231748569469235817`\
`178693452623457198945812736762984315314526987589371624857169243231748569496235871`\
`178693452623457198945812736786924315314586927592371684857169243231748569469235871`

Pass Condition\
Invalid Puzzle (“no unique solution”)

## Not Unique — 10 Solutions
This puzzle is not a valid Sudoku, because it has ten possible solutions.

Givens
```
5 9 . | . . . | . 4 8
6 . 8 | . . . | 3 . 7
. . . | 2 . 1 | . . .
------+-------+------
. . . | . 4 . | . . .
. 7 5 | 3 . 6 | 9 8 .
. . . | . 9 . | . . .
------+-------+------
. . . | 8 . 3 | . . .
2 . 6 | . . . | 7 . 9
3 4 . | . . . | . 6 5
```
`59.....486.8...3.7...2.1.......4.....753.698.....9.......8.3...2.6...7.934.....65`

Solution\
`592637148618459327437281596923748651175326984864195273759863412286514739341972865`\
`592637148618459327437281596963748251175326984824195673759863412286514739341972865`\
`592637148618459327734281596129748653475326981863195274957863412286514739341972865`\
`592637148618459327734281596129748653475326981863195472957863214286514739341972865`\
`592637148618459327734281596169748253475326981823195674957863412286514739341972865`\
`592637148618459327734281596829145673175326984463798251957863412286514739341972865`\
`592637148618459327734281596829145673475326981163798254957863412286514739341972865`\
`592637148618459327734281596829145673475326981163798452957863214286514739341972865`\
`592637148618459327734281596869145273175326984423798651957863412286514739341972865`\
`592637148618459327734281596869145273475326981123798654957863412286514739341972865`

Pass Condition\
Invalid Puzzle (“no unique solution”)

## Not Unique — 125 Solutions
This puzzle is not a valid Sudoku, because it has 125 possible solutions.

Givens
```
. . . | 3 1 6 | 5 . .
8 . . | 5 . . | 1 . .
. 1 . | 8 9 7 | 2 4 .
------+-------+------
9 . 1 | . 8 5 | . 2 .
. . . | 9 . 1 | . . .
. 4 . | 2 6 3 | . . 1
------+-------+------
. 5 . | . . . | . 1 .
1 . . | 4 . 9 | . . 2
. . 6 | 1 . 8 | . . .
```
`...3165..8..5..1...1.89724.9.1.85.2....9.1....4.263..1.5.....1.1..4.9..2..61.8...`

Solution\
`274316589893524167615897243931785426562941378748263951359672814187459632426138795`\
`274316589893524167615897243931785426762941358548263791359672814187459632426138975`\
`274316589893524167615897243931785426762941358548263971359672814187459632426138795`\
`274316589893524167615897243931785426762941835548263791459672318187439652326158974`\
`274316589893524167615897243931785426762941835548263971459672318187439652326158794`\
(additional solutions omitted for brevity)
`724316598869524173315897246931785624682941357547263981458632719173459862296178435`\
`724316598869524173315897246931785624682941735547263981453672819178459362296138457`\
`724316598869524173315897246931785624682941735547263981458672319173459862296138457`\
`724316598893524176615897243961785324382941765547263981459632817138479652276158439`\
`724316598893524176615897243961785324382941765547263981459632817178459632236178459`

Pass Condition\
Invalid Puzzle (“no unique solution”)
