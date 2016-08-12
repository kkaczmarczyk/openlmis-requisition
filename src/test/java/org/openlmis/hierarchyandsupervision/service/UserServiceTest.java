package org.openlmis.hierarchyandsupervision.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.hierarchyandsupervision.domain.User;
import org.openlmis.hierarchyandsupervision.repository.UserRepository;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@Transactional
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  @Autowired
  private UserService userService;

  private Integer currentInstanceNumber;
  private List<User> users;

  @Before
  public void setUp() {
    users = new ArrayList<>();
    currentInstanceNumber = 0;
    userService = new UserService();
    generateInstances();
    initMocks(this);
    mockRepositories();
  }

  @Test
  public void testSearchUsers() {
    List<User> receivedUsers = userService.searchUsers(
            users.get(0).getUsername(),
            users.get(0).getFirstName(),
            users.get(0).getLastName(),
            users.get(0).getHomeFacility(),
            users.get(0).getActive(),
            users.get(0).getVerified());

    Assert.assertEquals(1,receivedUsers.size());
    Assert.assertEquals(
            receivedUsers.get(0).getUsername(),
            users.get(0).getUsername());
    Assert.assertEquals(
            receivedUsers.get(0).getFirstName(),
            users.get(0).getFirstName());
    Assert.assertEquals(
            receivedUsers.get(0).getLastName(),
            users.get(0).getLastName());
    Assert.assertEquals(
            receivedUsers.get(0).getHomeFacility().getId(),
            users.get(0).getHomeFacility().getId());
    Assert.assertEquals(
            receivedUsers.get(0).getHomeFacility().getActive(),
            users.get(0).getActive());
    Assert.assertEquals(
            receivedUsers.get(0).getVerified(),
            users.get(0).getVerified());
  }

  private void generateInstances() {
    for (int instancesCount = 0; instancesCount < 5; instancesCount++) {
      users.add(generateUser());
    }
  }

  private User generateUser() {
    User user = new User();
    Integer instanceNumber = generateInstanceNumber();
    user.setFirstName("Ala" + instanceNumber);
    user.setLastName("ma" + instanceNumber);
    user.setUsername("kota" + instanceNumber);
    user.setPassword("iDobrze" + instanceNumber);
    user.setHomeFacility(generateFacility());
    user.setVerified(true);
    user.setActive(true);
    return user;
  }

  private Facility generateFacility() {
    Integer instanceNumber = + generateInstanceNumber();
    GeographicLevel geographicLevel = generateGeographicLevel();
    GeographicZone geographicZone = generateGeographicZone(geographicLevel);
    FacilityType facilityType = generateFacilityType();
    Facility facility = new Facility();
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setCode("FacilityCode" + instanceNumber);
    facility.setName("FacilityName" + instanceNumber);
    facility.setDescription("FacilityDescription" + instanceNumber);
    facility.setActive(true);
    facility.setEnabled(true);
    return facility;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode("GeographicLevel" + generateInstanceNumber());
    geographicLevel.setLevelNumber(1);
    return geographicLevel;
  }

  private GeographicZone generateGeographicZone(GeographicLevel geographicLevel) {
    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("GeographicZone" + generateInstanceNumber());
    geographicZone.setLevel(geographicLevel);
    return geographicZone;
  }

  private FacilityType generateFacilityType() {
    FacilityType facilityType = new FacilityType();
    facilityType.setCode("FacilityType" + generateInstanceNumber());
    return facilityType;
  }

  private Boolean checkIfUserMatchCriteria(User userModel, User userToCheck) {
    if (!userModel.getUsername().equalsIgnoreCase(userToCheck.getUsername())) {
      return false;
    }
    if (!userModel.getFirstName().equalsIgnoreCase(userToCheck.getFirstName())) {
      return false;
    }
    if (!userModel.getLastName().equalsIgnoreCase(userToCheck.getLastName())) {
      return false;
    }
    if (userModel.getHomeFacility().getId() != userToCheck.getHomeFacility().getId()) {
      return false;
    }
    if (userModel.getActive() != userToCheck.getActive()) {
      return false;
    }
    if (userModel.getVerified() != userToCheck.getVerified()) {
      return false;
    }
    return true;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }

  private void mockRepositories() {
    for (User user : users) {
      when(userRepository
              .findOne(user.getId()))
              .thenReturn(user);
    }
    for (User user : users) {
      when(userRepository
              .save(user))
              .thenReturn(user);
    }
    for (User user : users) {
      List<User> matchedUsers = new ArrayList<>();
      for (User userWithMatchedParameters : users) {
        Boolean isUserMatched = checkIfUserMatchCriteria(user, userWithMatchedParameters);
        if (isUserMatched) {
          matchedUsers.add(userWithMatchedParameters);
        }
      }
      when(userRepository
              .searchUsers(
                      user.getUsername(),
                      user.getFirstName(),
                      user.getLastName(),
                      user.getHomeFacility(),
                      user.getActive(),
                      user.getActive()))
              .thenReturn(matchedUsers);
    }
  }
}