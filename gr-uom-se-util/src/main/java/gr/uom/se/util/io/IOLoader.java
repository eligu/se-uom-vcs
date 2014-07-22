/**
 * 
 */
package gr.uom.se.util.io;

import gr.uom.se.util.validation.ArgsCheck;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class IOLoader {

   /**
    * The default size of the temp buffer
    */
   public static final int DEFAULT_BUFFER_SZ = 8192;
   /**
    * The default chunk of reads from stream
    */
   public static final int MIN_CHUNK_SZ = 1024;
   /**
    * The maximum chunk size
    */
   public static final int MAX_CHUNK_SZ = 4096;
   /**
    * The default maximum memory size
    */
   public static final int DEFAULT_MAX_MEMORY_SZ = 4096 * DEFAULT_BUFFER_SZ;

   /**
    * Load all bytes of the given input into memory.
    * <p>
    * 
    * This will maintain a default buffer at size {@link #DEFAULT_BUFFER_SZ}
    * where data will be written. If the stream has more than
    * {@link #DEFAULT_MAX_MEMORY_SZ} bytes this method will throw an I/O
    * exception.
    * 
    * See {@link #loadInMemory(InputStream, int, int)} if you want to control
    * the buffer size and the maximum memory allowed.
    * 
    * @param input
    *           the stream to read from
    * @return a new stream that contains all data loaded into memory
    * @throws IOException
    *            if a I/O problem occurs or there is not enough memory to
    *            contain the data.
    */
   public ByteArrayInputStream loadInMemory(InputStream input)
         throws IOException {
      return loadInMemory(input, DEFAULT_BUFFER_SZ, DEFAULT_MAX_MEMORY_SZ);
   }

   /**
    * Load all bytes of the given input into memory.
    * <p>
    * 
    * This will use a chunk size at 1/4 of the buffer size. if the chunk size is
    * too small it will use a default size at {@link #MIN_CHUNK_SZ}, unless the
    * buffer size is smaller than the {@link #MIN_CHUNK_SZ} so it will use a
    * buffer of that size.
    * <p>
    * Note that, the maximum memory provided here has no limit so if it is too
    * large and the data stream has a large number of bytes it may cause the
    * heap to be out of memory.
    * 
    * @param input
    *           the stream to read from
    * @param bufferSize
    *           the size in bytes of buffer to save bytes read temporary
    * @param maxMemory
    *           maximum allowed size of bytes read from stream
    * @return a new stream that contains all data loaded into memory
    * @throws IOException
    *            if a I/O problem occurs or there is not enough memory to
    *            contain the data.
    */
   public ByteArrayInputStream loadInMemory(InputStream input, int bufferSize,
         int maxMemory) throws IOException {

      ArgsCheck.notNull("input", input);
      ArgsCheck.greaterThan("bufferSize", "0", bufferSize, 0);
      ArgsCheck.greaterThan("maxMemory", "0", maxMemory, 0);
      ArgsCheck.lessThanOrEqual("bufferSize", "maxMemory", bufferSize,
            maxMemory);

      if (input instanceof ByteArrayInputStream) {
         return (ByteArrayInputStream) input;
      }

      // Try to set a chunk at 1/4 of buffer
      int chunk = bufferSize / 4;
      // If chunk is too small try to set it
      // at the minimum 1024
      if (chunk < MIN_CHUNK_SZ) {
         chunk = MIN_CHUNK_SZ;
      }
      // If buffer is smaller than default chunk
      if (bufferSize < MIN_CHUNK_SZ) {
         // Try to set the buffer at the size of the default chunk
         bufferSize = MIN_CHUNK_SZ;
         // If max memory is less than chunk
         if (bufferSize > maxMemory) {
            // Set the buffer at the size of max memory
            // and set the chunk at the same size
            bufferSize = maxMemory;
            chunk = bufferSize;
         }
      }

      // Allow chunks not greater than 4K
      if (chunk > MAX_CHUNK_SZ) {
         chunk = MAX_CHUNK_SZ;
      }

      byte[] buff = new byte[bufferSize];
      int len = 0;
      int read = 0;
      while ((read = input.read(buff, len, chunk)) != -1) {
         len += read;
         // Here check if we can read a chunk without adjusting
         // the buffer.
         int newSz = len + chunk;
         if (newSz > bufferSize) {
            // We should extend the buffer here
            // Because we can not read a whole chunk
            // Try first to extend the buffer by a chunk
            newSz = bufferSize + chunk;
            // Check memory limit
            if (newSz > maxMemory) {
               // Can not extend by a chunk because we exceed memory limit
               // We will try to extend by len + chunk so we can read only
               // a chunk before next extension
               newSz = len + chunk;
               // Check limit
               if (newSz > maxMemory) {
                  // We can not extend by a chunk from the current length
                  // So we make the last try setting the new buffer size to
                  // the maximum memory required
                  newSz = maxMemory;
                  // And the chunk size to the difference
                  chunk = maxMemory - len;

                  // If the chunk is less then 1 that means the memory is full
                  // and there are still bytes to read, so we throw an exception
                  throw new IOException("buffer size exceeds memory limit: "
                        + maxMemory);
               }
            }
            bufferSize = newSz;
            buff = Arrays.copyOf(buff, bufferSize);
         }
      }
      return new ByteArrayInputStream(buff, 0, len);
   }
}
