# Coders Strike Back

This is my approach to the [Coders Strike Back](https://www.codingame.com/multiplayer/bot-programming/coders-strike-back) challenge on [Codingame](https://www.codingame.com/home). Here is a description about the challenge.

> This puzzle game starts with a step by step tutorial that will help you get familiar with CodinGame’s multiplayer games. It provides an easy introduction to bot programming through a starship race.
>
> The aim of the game is of course to win the race against other players! To succeed in this challenge, you will be able to use different mathematical concepts such as trajectory calculation, collisions, speed vector, or inertia.
>
> The game is very simple to start. Rules are easy to understand and it only requires a few lines of code to move your ship around.
>
> However, it has near-infinite possibilities of evolution as you can improve your artificial intelligence step by step, while sharpening your coding skills

This challenge evolves as you progress through the tiers. The basic premise is the same throughout. The bot is provided with the state of the game every frame and must determine how the pod must move. The first pod to reach the final checkpoint wins.

The source code for this bot (Java) can be found at my [GitHub repository](https://github.com/avanderw/codingame/tree/master/coders-strike-back). 

## Up to Bronze

All the code is provided to you in some form or another to get to Wood 1. Along the way you will be introduced to the basics of:

- how to target the next checkpoint
- how to control the `thrust` of the pod
- how to consider the `thrust` relative to the heading

This will get you to Wood 1. In Wood 1 you are introduced to the concept of `BOOST`. There is one boost per round. Using it randomly is not enough to get you promoted to Bronze. To get to Bronze you need to consider:

- Is the `BOOST` available?
- Are we aligned with our target?
- Is the target arbitrarily far enough to justify the `BOOST`?

## Bronze to Gold

The solution to get to Gold made use of a heuristic. The heuristic used to get passed Bronze was submitted without any changes to pass Silver as well. Take into account that Silver tier introduced a new gameplay mechanic as well. Neither of the two gameplay mechanics introduced were considered in the heuristic. The gameplay mechanics were:

- Bronze tier introduced collision between pods.
- Silver tier introduced shields to increase the effect a collision has on your opponent.

Three attributes were used in the heuristic. The `thrust` of the pod. When to  `boost`. Lastly the `target` coordinate to thrust towards.

### Thrust

The below code handles the thrust heuristic.

```java
int k = 3;
double angleModifier = clamp(1 - (Math.abs(nextCheckpointAngle) / 180d), 0, 1);
double distanceModifier = clamp(nextCheckpointDist / (k * checkpointRadius), 0, 1);

double thrust = maxThrust * angleModifier * distanceModifier;
```

The `angleModifier` is used to reduce the `thrust`. It is a linear interpolation between full thrust (`nextCheckpointAngle == 0`) to no thrust (`nextCheckpointAngle == 180`). Essentially, you do not want to thrust when you are not facing towards the next checkpoint.

The `distanceModifier` is used to slow down when nearing the checkpoint. The `nextCheckpointDist` was used to linearly interpolate between `k * checkpointRadius` and `0` when nearing the checkpoint. An arbitrary factor of `k = 3` was used. This effectively started slowing the pod when it came within `3 * checkpointRadius` of the checkpoint.

### Boost

A boost would be used when all of these conditions were met:

- When there is sufficient distance (`nextCheckpointDist > 3600`) to boost
- When the final thrust is high enough (`thrust > 90`) as it implicitly accounts for angle
- When there is an available boost (`boostCount != 0`)

### Target

The pod would overshoot the checkpoint due to inertia pulling the pod off target. Slowing down for the checkpoint helped with this behavior, though not enough. Instead of trying to deal with the inertia using only thrust, I decided to change the target the pod was aiming for. The below code was used.

```java
int myDiffX = x - prevX;
int myDiffY = y - prevY;
int targetX = nextCheckpointX - myDiffX;
int targetY = nextCheckpointY - myDiffY;
```

What it does is keep track of the previous pod position. This can then be used to determine how much the pod moved between iterations. The movement on each axis is then subtracted from the next checkpoint. This has the effect of targeting the inner part of the corner expecting the inertia to drift the pod through the checkpoint.

## Gold

I have yet to submit a bot for the Gold tier. There are two major changes that need to be accounted for:

- You control two pods instead of one.
- Additionally, you receive exhaustive game information as input.

I do not expect any further gameplay changes. Therefore I will be designing a more robust solution that makes use of some AI technique like minmax, a genetic algorithm, a neural network, monte carlo, etc.







