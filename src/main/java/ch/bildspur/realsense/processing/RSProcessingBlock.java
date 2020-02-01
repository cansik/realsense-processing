package ch.bildspur.realsense.processing;

import org.intel.rs.processing.ProcessingBlock;

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

    public boolean isEnabled() {
        return enabled;
    }

    public T getBlock() {
        return block;
    }
}
