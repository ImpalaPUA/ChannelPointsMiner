package fr.raksrinana.channelpointsminer.miner.prediction.bet.outcome;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.raksrinana.channelpointsminer.miner.api.ws.data.message.subtype.Outcome;
import fr.raksrinana.channelpointsminer.miner.database.IDatabase;
import fr.raksrinana.channelpointsminer.miner.database.NoOpDatabase;
import fr.raksrinana.channelpointsminer.miner.database.model.prediction.OutcomeStatistic;
import fr.raksrinana.channelpointsminer.miner.handler.data.BettingPrediction;
import fr.raksrinana.channelpointsminer.miner.prediction.bet.exception.BetPlacementException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import java.util.Comparator;

@JsonTypeName("mostTrusted")
@Getter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class MostTrustedPicker implements IOutcomePicker{
	
	@Builder.Default
	private int minTotalBetsPlacedByUser = 5;
	@Builder.Default
	private int minTotalBetsPlacedOnPrediction = 10;
	@Builder.Default
	private int minTotalBetsPlacedOnOutcome = 5;
	
	@Override
	@NotNull
	public Outcome chooseOutcome(@NotNull BettingPrediction bettingPrediction, @NotNull IDatabase database) throws BetPlacementException{
		
		try{
			if(database instanceof NoOpDatabase){
				throw new BetPlacementException("A database needs to be configured for this outcome picker to work");
			}
			
			var outcomes = bettingPrediction.getEvent().getOutcomes();
			var title = bettingPrediction.getEvent().getTitle();
			
			var outcomeStatistics = database.getOutcomeStatisticsForChannel(bettingPrediction.getEvent().getChannelId(), minTotalBetsPlacedByUser);
			
			var mostTrusted = outcomeStatistics.stream()
					.max(Comparator.comparingDouble(OutcomeStatistic::getAverageReturnOnInvestment))
					.orElseThrow(() -> new BetPlacementException("No outcome statistics found. Maybe not enough data gathered yet."));
			
			for(var outcomeStats : outcomeStatistics){
				log.info("Outcome stats for '{}': {}", outcomeStats.getBadge(), outcomeStats.toString());
			}
			
			int totalBetsPlaced = outcomeStatistics.stream().mapToInt(OutcomeStatistic::getUserCnt).sum();
			if(totalBetsPlaced < minTotalBetsPlacedOnPrediction){
				throw new BetPlacementException("Not enough bets placed for prediction %s. Minimum is %d. Was %d".formatted(title, minTotalBetsPlacedOnPrediction, totalBetsPlaced));
			}
			
			var chosenOutcome = outcomes.stream()
					.filter(o -> o.getBadge().getVersion().equalsIgnoreCase(mostTrusted.getBadge()))
					.findAny()
					.orElseThrow(() -> new BetPlacementException("Outcome badge not found: %s".formatted(mostTrusted.getBadge())));
			
			if(mostTrusted.getUserCnt() < minTotalBetsPlacedOnOutcome){
				throw new BetPlacementException(
						"Not enough bets placed on chosen outcome: '%s'. Minimum is %d. Was %d".formatted(chosenOutcome.getTitle(), minTotalBetsPlacedOnOutcome, mostTrusted.getUserCnt()));
			}
			
			log.info("Prediction: '{}'. Most trusted outcome (highest average return of investment of other bettors): Title: '{}', Badge: {}.",
					title, chosenOutcome.getTitle(), chosenOutcome.getBadge().getVersion());
			
			return chosenOutcome;
		}
		catch(BetPlacementException e){
			throw e;
		}
		catch(Exception e){
			throw new BetPlacementException("Bet placement failed", e);
		}
	}
}