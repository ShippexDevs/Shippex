package com.shippex.constants;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.shippex.serialization.EnumNameDeserializer;
import com.shippex.serialization.EnumNameSerializer;

@JsonSerialize(using = EnumNameSerializer.class)
@JsonDeserialize(using = EnumNameDeserializer.class)
public enum Designation {
    MASTER,
    CHIEF_OFFICER,
    SECOND_OFFICER,
    THIRD_OFFICER,
    DECK_CADET,
    BOSUN,
    ABLE_SEAMAN,
    ORDINARY_SEAMAN,

    CHIEF_ENGINEER,
    SECOND_ENGINEER,
    THIRD_ENGINEER,
    FOURTH_ENGINEER,
    JUNIOR_ENGINEER,
    ENGINE_CADET,
    OILER,
    WIPER,
    MOTORMAN,
    FITTER,

    ELECTRO_TECHNICAL_OFFICER,
    ELECTRICIAN,

    CHIEF_COOK,
    SECOND_COOK,
    MESSMAN,
    STEWARD,
    CHIEF_STEWARD,

    PUMPMAN
}
