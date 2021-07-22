package curfew.service;

import curfew.dao.TokenApproversDAO;
import curfew.model.AccountIdentifierType;
import curfew.model.TokenApprover;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenApproverService {
  private final TokenApproversDAO tokenApproversDAO;

  public TokenApproverService(TokenApproversDAO tokenApproversDAO) {
    this.tokenApproversDAO = tokenApproversDAO;
  }

  public void makeEntries(List<String> lines) {
    for (String line : lines.subList(1, lines.size())) {
      String[] cols = line.split(",");
      tokenApproversDAO
          .insert(cols[0], AccountIdentifierType.valueOf(cols[1]), cols[2])
          .toCompletableFuture()
          .join();
    }
  }

  public TokenApprover getEntryOrNull(String identifier) {
    return tokenApproversDAO.getApprover(identifier).toCompletableFuture().join();
  }
}
