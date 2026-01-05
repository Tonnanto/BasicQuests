package de.stamme.basicquests.model.generation;

import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public class GenerationConfig {
  private final int default_min;
  private final int default_max;
  private final int default_step;
  private final double value_per_unit;
  private final List<GenerationOption> options;

  public GenerationConfig(
      int default_min,
      int default_max,
      int default_step,
      double value_per_unit,
      List<GenerationOption> options) {
    this.default_min = default_min;
    this.default_max = default_max;
    this.default_step = default_step;
    this.value_per_unit = value_per_unit;
    this.options = options;
  }

  @Override
  public GenerationConfig clone() {
    List<GenerationOption> options = null;
    if (this.options != null && !this.options.isEmpty()) {
      options = this.options.stream().map(GenerationOption::clone).collect(Collectors.toList());
    }
    return new GenerationConfig(default_min, default_max, default_step, value_per_unit, options);
  }

  public int getDefault_min() {
    return default_min;
  }

  public int getDefault_max() {
    return default_max;
  }

  public int getDefault_step() {
    return default_step;
  }

  public double getValue_per_unit() {
    return value_per_unit;
  }

  @Nullable
  public List<GenerationOption> getOptions() {
    return options;
  }
}
