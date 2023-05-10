package net.thenextlvl.worlds.image;

import com.google.gson.annotations.SerializedName;
import core.annotation.FieldsAreNullableByDefault;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldsAreNullableByDefault
public class WorldLinks {
    @SerializedName("the-end")
    public String end;
    public String nether;
    public String overworld;
}
