// Model/Cards/WaterRiseCard.java
package Model.Cards;

import Model.Enumeration.CardType;
import View.MainView;
import Model.WaterLevel;

public class WaterRiseCard extends Card {
    public WaterRiseCard() {
        super(CardType.WATER_RISE, "WaterRise", "水位上升一格");
    }

    @Override
    public void use() {
        WaterLevel.increase();
    }
}