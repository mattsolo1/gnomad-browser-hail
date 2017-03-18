// in file launch.scala
package launch {
  class Booster3
  object Booster3 {
    val thrust = 9876
  }
}

// in file rockets.scala
package rockets {
  package navigation {
    // In package rockets.navigation
    class Navigator {
      // No need to say rockets.navigation.StarMap
      val map = new StarMap
    }

    object StarMap {
      val location = 1234
    }
    class StarMap

    package launch {
      object Booster1 {
        val thrust = 1337
      }
      class Booster1
    }

    class MissionControl
    object MissionControl {
      val booster1 = launch.Booster1
      val booster2 = rockets.launch.Booster2
      val booster3 = _root_.launch.Booster3
    }

    package tests {
      // In package rockets.navigation.tests
      class NavigatorSuite
    }
  }
  object launch {
    class Booster2
    object Booster2 {
      val thrust = 42
    }
  }
  class launch

  class Ship {
    // no need to say rockets.navigation.navigator
    val nav = new navigation.Navigator
  }
}
