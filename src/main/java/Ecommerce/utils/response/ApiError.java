package Ecommerce.utils.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Standard error shape for every 4xx/5xx response, replacing the previous
 * ad-hoc per-controller try/catch bodies (which sometimes returned raw
 * exception messages to the client). See GlobalExceptionHandler.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private Instant timestamp;
    private int status;
    private String code;
    private String message;
    private String path;
    private List<String> errors;
}
