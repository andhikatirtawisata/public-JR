# Project 3 Design Document

## Java classes and Data Structures

### World class

Responsible for creating everything in the world, delegate works to other classes (e.g. Room, Hall).

* It has world tiles and a list of rooms.
* Primary method: constructor
  * Initialize an empty world
  * Randomly a number of rooms
  * Keep ArrayList of created rooms
  * For each new room, create hallway to nearby rooms
* Create other features:  avatar, lighting, and door etc.

### Room class

* Keep location x,y limits
* Size of room: height, width
* Other features: lighting
* Add wall upon generation

### Hallway class:
* Keep start point of a hallway (x,y)
* Build hallway between two points
* Simply set all tiles along hallway to floor, which has an effect breaking wall into a room as well.
* Don't build hallway wall initially
* After the world is built, add wall for any floor not protected by wall.

### Position class
* Keep a (x,y) position on the board
* A list of utility helper methods, such as check whether a position is boarder, computer 
distance between 2 positions. etc.

### Interact class
* Handle all user interactions
* Show menu and process menu events
* Save and load world
* Handle keyboard movements (and movement input string) in the world
* Turn on/off light: action to step into a switch will turn on/off a light
* Detect end of nagivation game (to exit door)

### Data Structures
* ArrayList: keep a list of rooms in World
* WeightedQuickUnionUF: it's used to check connectivity from Avatar to exit door and also from Avatar to each room.
Tis will ensure the world we build is connected.

# Algorithms

## Build Rooms

Add a random number of rooms, with minimum and maximum number of rooms. Width and Height of 
rooms are randomly generated too (with minimum and bounds)

## Build Hallways
Upon generating a room, immediately create a hallway to nearest room. We also add 1 extra connection from the new room 
to another with 50% probability.

We only add floor in hallway and don't build hallway wall initially. Instead, after the world is built, add wall for 
any floor not protected by wall.

## World Connectivity check

We use WeightedQuickUnionUF to check Avatar and exit door is connected, we also check if Avatar can reach each room.

If there is any issue with connectivity, errors will be reported. 

## Turn on/off a light
Action to step into a light switch will turn on/off a light. When a light is turned on, we replace nearby 
regular floors with shining floors; similarly when a light is turned off, we replace nearby shinning
floor tiles with regular floors.

# Persistence
* We simply save random seed and action history string
* Save into proj3save.txt file in proj3 main directory
* During load, rebuild the world from random seed, then replay actions from saved action history string

# Ambition Points included

* Major feature: lighting, with the ability to turn on/off a light with key press.
* Secondary feature: The ability to create a new world without closing 
  and reopening the project, when reach a “game over” state.
* Secondary feature: real-time date and time in HUD
