# clout
The current implementation of this application uses Java and H2 relational DB currently used with in-memory mode for quick and clean setup and teardown. 
maven is used as the build tool and "mvn clean install" should generate an executable jar in thetarget directory and local mvn repo named "clout-1.0-SNAPSHOT-jar-with-dependencies.jar". 
For convenience, standard practice has been broken here and the target directory has been uploaded incase the user does not have maven setup to build.

TO RUN: simply run the script "clout" using "./clout"

REQUIREMENTS AND PROJECT BACKGROUND:

Definition:

influence or power, esp. in politics or business.
a heavy blow with the hand or a hard object.
Problem

I would like you to create a simple CLI that allows the user to:

Describe an asymmetrical social graph with a series of one-line commands.
Determine the extended influence of any particular person in the graph.
Spec

We define the follower/followee contract as follows:

A person can be followed zero or more people.
A person can follow zero or 1 person including themself.

Example

$ ./clout
Usage:

1. Add a relationship:
> <person_a> follows <person_b>

2. Determine extended influence of a person:
> clout <person>

3. Determine extended influence of all people in the graph:
> clout

4. Exit application
> exit

> Neymar follows Xavi

OK!

> clout Xavi

Xavi has 1 follower.

> Neymar follows Messi

OK! # Can change who a person is following.

> clout Xavi

Xavi has no followers.

> clout Messi

Messi has 1 follower.

> Messi follows Messi

Interesting, but that doesn't make sense.

> Pique follows Victor Valdes

OK!

> Jordi Alba follows Pique

OK!

> clout Victor Valdes

Victor Valdes has 2 followers.

> clout

Victor Valdes has 2 followers

Messi has 1 follower

Pique has 1 follower

Jordi Alba has no followers

Neymar has no followers

Xavi has no followers

> Messi follows Victor Valdes

OK!

> clout Victor Valdes

Victor Valdes has 4 followers # Valdes has inherited the followers that Messi had, plus Messi himself.
