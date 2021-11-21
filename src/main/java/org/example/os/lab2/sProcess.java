package org.example.os.lab2;

public class sProcess {
  public int cpu_time; // required total time
  public int io_blocking; // batch per one time
  public int cpu_done; // count of finished time
  public int io_next; // count of finished in batch
  public int num_blocked;

  public sProcess (int cpu_time, int io_blocking, int cpu_done, int io_next, int num_blocked) {
    this.cpu_time = cpu_time;
    this.io_blocking = io_blocking;
    this.cpu_done = cpu_done;
    this.io_next = io_next;
    this.num_blocked = num_blocked;
  }
}
