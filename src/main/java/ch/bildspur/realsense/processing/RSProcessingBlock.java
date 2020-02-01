package ch.bildspur.realsense.processing;

import org.intel.rs.processing.ProcessingBlock;
import org.intel.rs.types.Option;

public class RSProcessingBlock<T extends ProcessingBlock> {
    private boolean enabled;
    private T block;

    /**
     * Initialize a new processing block.
     * @param block Block to initialize.
     */
    public void init(T block) {
        this.block = block;

        this.enabled = true;
    }

    /**
     * Release the current block.
     */
    public void release() {
        if(enabled)
            block.release();
    }

    /**
     * Set option for block.
     * @param option Option to be set.
     * @param value Value to be set.
     */
    public void setOption(Option option, float value) {
        if(!enabled)
            throw new RuntimeException("Block has not been enabled!");

        block.getOptions().get(option).setValue(value);
    }

    /**
     * Reads value from block option.
     * @param option Option to read from.
     * @return Actual value of block.
     */
    public float getValue(Option option) {
        if(!enabled)
            throw new RuntimeException("Block has not been enabled!");

        return block.getOptions().get(option).getValue();
    }

    /**
     * Returns true if block has been enabled.
     * @return Returns true if block has been enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns current block.
     * @return Returns current block.
     */
    public T getBlock() {
        return block;
    }
}
