***2048: Engine and Solver in Java***

Warning: This is an "engine" for the game.
  There is no UI associated with it and is intended
  to be run on the command line.

This repo hosts a java implementation of the game 2048

I have also included a very rough min-max solver which
results in a win most of the time depending on the AI
parameters set.

This started out as an attempt at machine learning:
  I wanted to create a solver that would get better at
  estimating the best move based on trials and trials
  of reinforced learning using a Least-Squares
  Temporal Difference Learning algorithm.

  My plan was to get the AI smart enough where it
  could spend less time diving down in the min-max
  by approximating. I set a goal to create a solution
  that was as fast and as accurate as I could.

Also know as... how I spent my spring break.

Long story short, I spent most of my time optimizing
the time it takes to play a game, which would allow
me to run more games in a shorter amount of time.
In doing that, I ran out of time to finish the project
over my break and have not had time to resume yet.

There will be a day when I come back and finish this,
but for right now I'm deciding to host it on GitHub
in the event that someone can get use from looking
at where I was going.

I'm not going to be the best reference for machine
learning algorithms, but definitely feel free to
email me if you have any questions!
jamesfator@gmail.com
