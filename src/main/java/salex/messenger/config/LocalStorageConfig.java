package salex.messenger.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "storage")
public record LocalStorageConfig(@NotNull String location, @NotNull DataSize maxFileSize) {}
