package fr.raksrinana.channelpointsminer.api.gql.data.reportmenuitem;

import fr.raksrinana.channelpointsminer.api.gql.data.GQLOperation;
import fr.raksrinana.channelpointsminer.api.gql.data.GQLResponse;
import fr.raksrinana.channelpointsminer.api.gql.data.PersistedQueryExtension;
import kong.unirest.GenericType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@ToString
public class ReportMenuItemOperation extends GQLOperation<ReportMenuItemData>{
	public ReportMenuItemOperation(@NotNull String username){
		super("ReportMenuItem");
		addPersistedQueryExtension(new PersistedQueryExtension(1, "8f3628981255345ca5e5453dfd844efffb01d6413a9931498836e6268692a30c"));
		addVariable("channelLogin", username);
	}
	
	@Override
	@NotNull
	public GenericType<GQLResponse<ReportMenuItemData>> getResponseType(){
		return new GenericType<>(){};
	}
}