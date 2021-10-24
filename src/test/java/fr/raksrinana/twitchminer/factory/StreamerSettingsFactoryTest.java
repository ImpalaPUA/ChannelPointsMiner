package fr.raksrinana.twitchminer.factory;

import fr.raksrinana.twitchminer.config.Configuration;
import fr.raksrinana.twitchminer.miner.priority.*;
import fr.raksrinana.twitchminer.miner.streamer.StreamerSettings;
import fr.raksrinana.twitchminer.tests.TestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamerSettingsFactoryTest{
	private static final StreamerSettings DEFAULT = StreamerSettings.builder().build();
	private static final String STREAMER_USERNAME = "streamer-name";
	
	@TempDir
	private Path tempDir;
	
	@InjectMocks
	private StreamerSettingsFactory tested;
	
	@Mock
	private Configuration configuration;
	
	@BeforeEach
	void setUp(){
		when(configuration.getDefaultStreamerSettings()).thenReturn(DEFAULT);
		lenient().when(configuration.getStreamerConfigDirectory()).thenReturn(tempDir);
	}
	
	@Test
	void getDefaultConfiguration(){
		assertThat(tested.getDefaultSettings()).isSameAs(DEFAULT);
	}
	
	@Test
	void getStreamerConfigurationWithConfigFolderMissing(){
		when(configuration.getStreamerConfigDirectory()).thenReturn(tempDir.resolve("unknown-dir"));
		
		assertThat(tested.createStreamerSettings(STREAMER_USERNAME)).isNotSameAs(DEFAULT)
				.usingRecursiveComparison().isEqualTo(DEFAULT);
	}
	
	@Test
	void getStreamerConfigurationWithNoConfigFile(){
		assertThat(tested.createStreamerSettings(STREAMER_USERNAME)).isNotSameAs(DEFAULT)
				.usingRecursiveComparison().isEqualTo(DEFAULT);
	}
	
	@Test
	void getStreamerConfigurationWithConfigFileEmpty(){
		TestUtils.copyFromResources("factory/empty.json", tempDir.resolve(STREAMER_USERNAME + ".json"));
		
		assertThat(tested.createStreamerSettings(STREAMER_USERNAME)).isNotSameAs(DEFAULT)
				.usingRecursiveComparison().isEqualTo(DEFAULT);
	}
	
	@Test
	void getStreamerConfigurationWithConfigFileNothingRedefined(){
		TestUtils.copyFromResources("factory/nothingRedefined.json", tempDir.resolve(STREAMER_USERNAME + ".json"));
		
		assertThat(tested.createStreamerSettings(STREAMER_USERNAME)).isNotSameAs(DEFAULT)
				.usingRecursiveComparison().isEqualTo(DEFAULT);
	}
	
	@Test
	void getStreamerConfigurationWithConfigFilePartiallyRedefined(){
		TestUtils.copyFromResources("factory/partiallyOverridden.json", tempDir.resolve(STREAMER_USERNAME + ".json"));
		
		assertThat(tested.createStreamerSettings(STREAMER_USERNAME)).isNotSameAs(DEFAULT)
				.usingRecursiveComparison().isEqualTo(StreamerSettings.builder()
						.followRaid(true)
						.build());
	}
	
	@Test
	void getStreamerConfigurationWithConfigFileAllRedefined(){
		TestUtils.copyFromResources("factory/fullyOverridden.json", tempDir.resolve(STREAMER_USERNAME + ".json"));
		
		var priorities = new ArrayList<StreamerPriority>();
		priorities.add(ConstantPriority.builder()
				.score(50)
				.build());
		priorities.add(SubscribedPriority.builder()
				.score(100)
				.score2(200)
				.score3(300)
				.build());
		priorities.add(PointsAbovePriority.builder()
				.score(25)
				.threshold(10)
				.build());
		priorities.add(PointsBelowPriority.builder()
				.score(75)
				.threshold(20)
				.build());
		
		when(configuration.getDefaultStreamerSettings()).thenReturn(StreamerSettings.builder()
				.priorities(List.of(ConstantPriority.builder()
						.score(555)
						.build()))
				.build());
		
		var expected = StreamerSettings.builder()
				.makePredictions(true)
				.followRaid(true)
				.priorities(priorities)
				.build();
		
		assertThat(tested.createStreamerSettings(STREAMER_USERNAME)).isNotSameAs(DEFAULT)
				.isEqualTo(expected);
	}
}