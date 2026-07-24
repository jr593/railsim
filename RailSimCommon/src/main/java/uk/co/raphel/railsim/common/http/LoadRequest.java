package uk.co.raphel.railsim.common.http;

import uk.co.raphel.railsim.common.enums.LoadType;

public record LoadRequest(
      LoadType loadType,
      String fileName
) {
}
