package dev.alphasucks.aea.port.tick;

public final class TickState {
   public static final int INHERIT = -1;
   public static final long PERMANENT = -1L;
   private int rate = -1;
   private boolean frozen;
   private boolean exempt;
   private int stepTicks;
   private int sprintTicks;
   private long expiresAtTick = -1L;

   public int rate() {
      return this.rate;
   }

   public boolean frozen() {
      return this.frozen;
   }

   public boolean exempt() {
      return this.exempt;
   }

   public void setExempt(boolean exempt) {
      this.exempt = exempt;
   }

   public int stepTicks() {
      return this.stepTicks;
   }

   public int sprintTicks() {
      return this.sprintTicks;
   }

   public void setRate(int rate) {
      this.rate = rate < 0 ? -1 : rate;
   }

   public void setFrozen(boolean frozen) {
      this.frozen = frozen;
      if (frozen) {
         this.sprintTicks = 0;
      }
   }

   public void setStepTicks(int ticks) {
      this.stepTicks = Math.max(0, ticks);
   }

   public void setSprintTicks(int ticks) {
      this.sprintTicks = Math.max(0, ticks);
      if (this.sprintTicks > 0) {
         this.stepTicks = 0;
      }
   }

   public void keepUntil(long tick) {
      if (tick == -1L || this.expiresAtTick == -1L) {
         this.expiresAtTick = tick;
      } else if (tick > this.expiresAtTick) {
         this.expiresAtTick = tick;
      }
   }

   public void setExpiry(long tick) {
      this.expiresAtTick = tick;
   }

   public boolean expired(long tick) {
      return this.expiresAtTick != -1L && tick >= this.expiresAtTick;
   }

   public boolean isDefault() {
      return this.rate == -1 && !this.frozen && this.stepTicks == 0 && this.sprintTicks == 0 && !this.exempt;
   }

   public void consumeStep() {
      if (this.stepTicks > 0) {
         this.stepTicks--;
      }
   }

   public void consumeSprint() {
      if (this.sprintTicks > 0) {
         this.sprintTicks--;
      }
   }

   public int effectiveRate(int base) {
      if (this.sprintTicks > 0) {
         return base;
      } else if (this.frozen) {
         return this.stepTicks > 0 ? base : 0;
      } else {
         return this.rate == -1 ? base : Math.min(this.rate, base);
      }
   }

   public void countDown() {
      if (this.sprintTicks > 0) {
         this.sprintTicks--;
      } else if (this.frozen && this.stepTicks > 0) {
         this.stepTicks--;
      }
   }
}
