package fr.raksrinana.channelpointsminer.api.gql.data.inventory;

import fr.raksrinana.channelpointsminer.api.gql.data.GQLOperation;
import fr.raksrinana.channelpointsminer.api.gql.data.GQLResponse;
import fr.raksrinana.channelpointsminer.api.gql.data.PersistedQueryExtension;
import kong.unirest.GenericType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class InventoryOperation extends GQLOperation<InventoryData>{
	public InventoryOperation(){
		super("Inventory");
		addPersistedQueryExtension(new PersistedQueryExtension(1, "e0765ebaa8e8eeb4043cc6dfeab3eac7f682ef5f724b81367e6e55c7aef2be4c"));
	}
	
	@Override
	@NotNull
	public GenericType<GQLResponse<InventoryData>> getResponseType(){
		return new GenericType<>(){};
	}
}