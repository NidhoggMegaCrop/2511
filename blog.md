# Assignment 1

## Phase 1: Planning and Decision Making

From the spec, I identified four main entity types: Stations, Trains, Tracks, and Loads. Each type had multiple variations which suggested inheritance hierarchies.

My first major decision was choosing between abstract classes and interfaces for my entity hierarchies. I chose abstract classes as from 
reading the specs, I noticed that all station types share common state, and all train types share common state.

Using abstract classes allowed me to implement shared behavior once in the parent class, avoiding code duplication across the four station types.

### Route Design Challenge

Initially, I considered putting route logic directly in the Train class. However, after learning from my tutor I realised that this is a bad behavior as a train should handle movement and cargo, not route navigation logic. Therefore, I created separate Route classes that encapsulate route traversal.

---

## Phase 2: Coding Task A

### Challenge 1:

I initially wasn't sure whether to put all concrete classes in one file or separate files. After clarifying from my tutor, I understood that 
public classes  must each be in their own file with a matching name. Therefore, I created separate files:
- `PassengerStation.java` for `public class PassengerStation`
- `CargoStation.java` for `public class CargoStation`
- And so on for all concrete classes

### Challenge 2:

Implementing route validation was more complex than expected. I needed to:
1. Determine if a route is cyclical
2. Validate all consecutive stations have tracks
3. Throw `InvalidRouteException` if train type doesn't match route type

Therefore, I created helper methods to break down the complexity.

