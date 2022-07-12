package de.stamme.basicquests.questgeneration;

import java.util.List;

public class GenerationConfig {
    private final int default_min;
    private final int default_max;
    private final int default_step;
    private final List<GenerationOption> options;

    public GenerationConfig(int default_min, int default_max, int default_step, List<GenerationOption> options) {
        this.default_min = default_min;
        this.default_max = default_max;
        this.default_step = default_step;
        this.options = options;
    }

    public GenerationConfig(List<GenerationOption> options) {
        this.options = options;
        this.default_min = 0;
        this.default_max = 0;
        this.default_step = 0;
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

    public List<GenerationOption> getOptions() {
        return options;
    }
}

