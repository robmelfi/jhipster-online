package io.github.jhipster.online.service;

import io.github.jhipster.online.JhonlineApp;
import io.github.jhipster.online.domain.*;
import io.github.jhipster.online.repository.*;
import io.github.jhipster.online.service.util.DataGenerationUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JhonlineApp.class)
public class StatisticsServiceIntTest {

    private String generatorId = "cf51ff78-187a-4554-9b09-8f6f95f1a7a5";

    private String dummyYo = "{\n" +
        "        \"generator-jhipster\": {\n" +
        "        \"useYarn\": true,\n" +
        "            \"experimental\": false,\n" +
        "            \"skipI18nQuestion\": true,\n" +
        "            \"logo\": false,\n" +
        "            \"clientPackageManager\": \"yarn\",\n" +
        "            \"cacheProvider\": \"ehcache\",\n" +
        "            \"enableHibernateCache\": true,\n" +
        "            \"websocket\": false,\n" +
        "            \"databaseType\": \"sql\",\n" +
        "            \"devDatabaseType\": \"h2Disk\",\n" +
        "            \"prodDatabaseType\": \"mysql\",\n" +
        "            \"searchEngine\": false,\n" +
        "            \"messageBroker\": false,\n" +
        "            \"serviceDiscoveryType\": false,\n" +
        "            \"buildTool\": \"maven\",\n" +
        "            \"enableSwaggerCodegen\": false,\n" +
        "            \"authenticationType\": \"jwt\",\n" +
        "            \"serverPort\": \"8080\",\n" +
        "            \"clientFramework\": \"angularX\",\n" +
        "            \"useSass\": false,\n" +
        "            \"testFrameworks\": [],\n" +
        "        \"enableTranslation\": true,\n" +
        "            \"nativeLanguage\": \"en\",\n" +
        "            \"languages\": [\n" +
        "        \"en\"\n" +
        "      ],\n" +
        "        \"applicationType\": \"monolith\"\n" +
        "    },\n" +
        "        \"generator-id\": \"" + generatorId + "\",\n" +
        "        \"generator-version\": \"5.1.0\",\n" +
        "        \"git-provider\": \"local\",\n" +
        "        \"node-version\": \"v8.11.1\",\n" +
        "        \"os\": \"linux:4.15.0-29-generic\",\n" +
        "        \"arch\": \"x64\",\n" +
        "        \"cpu\": \"Intel(R) Core(TM) i7-7700K CPU @ 4.20GHz\",\n" +
        "        \"cores\": 8,\n" +
        "        \"memory\": 16776642560,\n" +
        "        \"user-language\": \"en_GB\",\n" +
        "        \"isARegeneration\": true\n" +
        "    }";


    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private YoRCRepository yoRCRepository;

    @Autowired
    private SubGenEventRepository subGenEventRepository;

    @Autowired
    private EntityStatsRepository entityStatsRepository;

    @Autowired
    private GeneratorIdentityRepository generatorIdentityRepository;

    @Autowired
    private UserRepository userRepository;

    private List<YoRC> yos;
    private List<SubGenEvent> subs;
    private List<EntityStats> entities;

    @Before
    public void init() {
        Calendar now = Calendar.getInstance();
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        DataGenerationUtil.clearYoRcTable(yoRCRepository);
        DataGenerationUtil.clearSubGenVentTable(subGenEventRepository);
        DataGenerationUtil.clearEntityStatsTable(entityStatsRepository);

        yos = DataGenerationUtil.addYosToDatabase(100, now, nextYear, yoRCRepository);
        subs = DataGenerationUtil.addSubGenEventsToDatabase(100, now, nextYear, subGenEventRepository);
        entities = DataGenerationUtil.addEntityStatsToDatabase(100, now, nextYear, entityStatsRepository);
    }


    @Test
    public void assertThatAddingAYoWorks() {
        try {
            statisticsService.addEntry(dummyYo, "127.0.0.1");
        } catch (IOException e) {
            // Test is a failure with this exception.
            assertThat(false).isTrue();
        }

        assertThat(yoRCRepository.count()).isEqualTo(yos.size() + 1);
    }

    @Test
    public void assertThatAddingASubGenEventWorks() {
        statisticsService.addSubGenEvent(new SubGenEvent(), generatorId);
        assertThat(subGenEventRepository.count()).isEqualTo(subs.size() + 1);
    }

    @Test
    public void assertThatAddingAAnEntityStatWorks() {
        statisticsService.addEntityStats(new EntityStats(), generatorId);
        assertThat(entityStatsRepository.count()).isEqualTo(entities.size() + 1);
    }

    @Test
    public void assertThatStatisticsCanBeDeleted() {
        User user = new User();
        user.setLogin("johndoe");
        user.setPassword(RandomStringUtils.random(60));
        userRepository.save(user);

        IntStream.range(0, 10).forEach(i -> {
            statisticsService.addSubGenEvent(new SubGenEvent(), generatorId);
            statisticsService.addEntityStats(new EntityStats(), generatorId);
            try {
                statisticsService.addEntry(dummyYo, "127.0.0.1");
            } catch (IOException e) {
                // Test is a failure with this exception.
                assertThat(false).isTrue();
            }
        });

        GeneratorIdentity generatorIdentity = generatorIdentityRepository.save(
            generatorIdentityRepository.findFirstByGuidEquals(generatorId).get().owner(user)
        );

        statisticsService.deleteStatistics(user);

        assertThat(
            yoRCRepository.findAll().stream()
                .noneMatch(yo -> yo.getOwner() != null &&
                    yo.getOwner().equals(generatorIdentity))
        ).isTrue();
        assertThat(
            subGenEventRepository.findAll().stream()
                .noneMatch(sub -> sub.getOwner() != null &&
                    sub.getOwner().equals(generatorIdentity))
        ).isTrue();
        assertThat(
            entityStatsRepository.findAll().stream()
                .noneMatch(entity -> entity.getOwner() != null &&
                    entity.getOwner().equals(generatorIdentity))
        ).isTrue();
    }

}
