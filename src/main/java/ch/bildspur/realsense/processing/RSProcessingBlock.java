package ch.bildspur.realsense.processing;

import org.intel.rs.processing.ProcessingBlock;
import org.intel.rs.types.Option;

public class RSProcessingBlock<T extends ProcessingBlock> {
    private boolean enabled;
    private T block;

    public void init(T block) {
        this.block = block;

        this.enabled = true;
    }

    public void release() {
        if(enabled)
            block.release();
    }

    public void setOption(Option option, float value) {
        if(!enabled)
            throw new RuntimeException("Block has not been enabled!");

        block.getOptions().get(option).setValue(value);
    }

    public float getValue(Option option) {
        if(!enabled)
            throw new RuntimeException("Block has not been enabled!");

        return block.getOptions().get(option).getValue();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public T getBlock() {
        return block;
    }
}
