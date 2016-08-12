package org.openlmis.settings.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.settings.exception.ConfigurationSettingException;
import org.openlmis.settings.repository.ConfigurationSettingRepository;
import org.openlmis.settings.service.ConfigurationSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@Transactional
public class ConfigurationSettingServiceTest {

  private ConfigurationSetting setting;

  @Mock
  private ConfigurationSettingRepository configurationSettingRepository;

  @InjectMocks
  @Autowired
  private ConfigurationSettingService configurationSettingService;

  @Before
  public void setUp() {
    setting = new ConfigurationSetting();
    initMocks(this);
    mockRepositories();
  }

  @Test
  public void testGetStringValue() throws ConfigurationSettingException {
    setting.setKey("testString");
    setting.setValue("testValue");
    Assert.assertTrue(configurationSettingService.getStringValue("testString").equals("testValue"));
  }

  private void mockRepositories() {
    when(configurationSettingRepository
        .findOne(setting.getKey()))
        .thenReturn(setting);
    /*when(configurationSettingRepository
        .save(stock))
        .thenReturn(stock);*/
  }
}
