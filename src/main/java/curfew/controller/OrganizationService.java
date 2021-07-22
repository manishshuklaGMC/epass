package curfew.controller;

import curfew.dao.OrganizationDAO;
import curfew.exception.CurfewPassException;
import curfew.model.Organization;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletionStage;

@Service
public class OrganizationService {
  private OrganizationDAO organizationDAO;

  public OrganizationService(OrganizationDAO organizationDAO) {
    this.organizationDAO = organizationDAO;
  }

  public String putOrganization(Organization organization) {
    return organizationDAO.insertOrganization(organization).toCompletableFuture().join();
  }

  public CompletionStage<Organization> getOrganization(String organizationID, Integer stateID) {
    return organizationDAO.getOrganizationByID(organizationID, stateID);
  }

  public CompletionStage<List<Organization>> getAllOrganizations(Integer stateID) {
    return organizationDAO.getAllOrganizations(stateID);
  }

  public CompletionStage<Void> updateActivePassLimit(Integer idOrganization, Integer newLimit) {
    return organizationDAO
        .updateLimit(idOrganization, newLimit)
        .thenApply(
            updateCount -> {
              if (updateCount == 0) {
                throw new CurfewPassException("Invalid organaization ID");
              }
              return null;
            });
  }
}
