package rockets

import org.scalatest._

class RocketSpec extends FlatSpec with Matchers {
  "Navigation package" should "have a star map location" in {
    navigation.StarMap.location shouldEqual 1234
  }
  "Boosters" should "have correct value" in {
    navigation.MissionControl.booster1.thrust shouldEqual 1337
    navigation.MissionControl.booster2.thrust shouldEqual 42
    navigation.MissionControl.booster3.thrust shouldEqual 9876
  }
}
