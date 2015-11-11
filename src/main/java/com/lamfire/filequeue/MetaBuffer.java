package com.lamfire.filequeue;

import com.lamfire.logger.Logger;
import com.lamfire.utils.Bytes;
import com.lamfire.utils.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 队列元数据读写
 * @author lamfire
 *
 */
class MetaBuffer {
    private static final Logger LOGGER = Logger.getLogger(MetaBuffer.class);
	public static final String FILE_SUFFIX = "";
	public static final int META_FILE_LENGTH = 64;

    public static String getMetaFileName(String dir,String name){
        dir = FilenameUtils.normalizeNoEndSeparator(dir);
        String fileName = (name + FILE_SUFFIX );
        return (dir+ File.separator + fileName);
    }

    public static File getMetaFile(String dir,String name){
        return new File(getMetaFileName(dir, name));
    }

    public static void deleteMetaFile(String dir,String name){
        File file = getMetaFile(dir, name);
        if(file.exists() && file.isFile()){
            file.delete();
        }
    }

    private final Lock lock = new ReentrantLock();
	private FileBuffer file = null;
	
	private final byte[] buffer = new byte[META_FILE_LENGTH];

    private final AtomicInteger readIndex = new AtomicInteger(0); //当前读取页
    private int readIndexOffset = 0;//当前读取位置

    private int readDataIndex = 0;
    private int readDataOffset = 0;

    private final AtomicInteger writeIndex = new AtomicInteger(0);//当前写页
    private int writeIndexOffset = 0;//当前写位置

    private final AtomicInteger writeDataIndex = new AtomicInteger(0); //最后写入Store 号
    private int writeDataOffset = 0;//最后写入数据位置

    private final int indexFilePartitionLength;   //索引文件分区大小
    private final int dataFilePartitionLength ;     //数据文件分区大小
	
	public MetaBuffer(File file,int indexFilePartitionLength , int dataFilePartitionLength) throws IOException{
		this.file = new FileBuffer(file,META_FILE_LENGTH);
        this.indexFilePartitionLength = indexFilePartitionLength;
        this.dataFilePartitionLength = dataFilePartitionLength;
        loadFromFile();
	}
	
	private void loadFromFile()throws IOException{
        try{
            lock.lock();
            this.readIndex.set(file.getInt(0));
            this.readIndexOffset = file.getInt(4);
            this.readDataIndex = file.getInt(8);
            this.readDataOffset = file.getInt(12);

            this.writeIndex.set(file.getInt(16));
            this.writeIndexOffset = file.getInt(20);
            this.writeDataIndex.set(file.getInt(24));
            this.writeDataOffset = file.getInt(28);
        }finally {
            lock.unlock();
        }
	}
	
	public void flush()throws IOException{
        try{
            lock.lock();
            Bytes.putInt(buffer,0, readIndex.get());
            Bytes.putInt(buffer,4, readIndexOffset);
            Bytes.putInt(buffer,8, readDataIndex);
            Bytes.putInt(buffer,12, readDataOffset);

            Bytes.putInt(buffer,16, writeIndex.get());
            Bytes.putInt(buffer,20,writeIndexOffset);
            Bytes.putInt(buffer,24, writeDataIndex.get());
            Bytes.putInt(buffer,28, writeDataOffset);
            Bytes.putInt(buffer,32, indexFilePartitionLength);
            Bytes.putInt(buffer,36, dataFilePartitionLength);
            file.setWritePosition(0);
            file.put(buffer);
        }finally {
            lock.unlock();
        }
	}
	
	public void clear()throws IOException{
        try{
            lock.lock();
            this.readIndex.set(0);
            this.readIndexOffset =0;
            this.readDataIndex = 0;
            this.readDataOffset = 0;

            this.writeIndex.set(0);
            this.writeIndexOffset = 0;
            this.writeDataIndex.set(0);
            this.writeDataOffset = 0;
            flush();
        }finally {
            lock.unlock();
        }
	}
	
	public void close(){
        try{
            lock.lock();
        if(this.file == null){
            return;
        }
		this.file.close();
		this.file = null;
        }finally {
            lock.unlock();
        }
	}

    public void closeAndDeleteFile(){
        try{
            lock.lock();
            if(this.file == null){
                return;
            }
            LOGGER.info("deleting meta file : " + file.getFilePath());
            this.file.closeAndDeleteFile();
            this.file = null;
        }finally {
            lock.unlock();
        }
    }

	public long getReadCount() {
        try{
            lock.lock();
            return (1l * indexFilePartitionLength * readIndex.get() + readIndexOffset) /  Element.ELEMENT_LENGTH;
        }finally {
            lock.unlock();
        }
	}


	public long getWriteCount() {
        try{
            lock.lock();
		    return (1l * indexFilePartitionLength * writeIndex.get() + writeIndexOffset) /  Element.ELEMENT_LENGTH;
        }finally {
            lock.unlock();
        }
	}

    public int getReadIndex() {
        try{
            lock.lock();
            return readIndex.get();
        }finally {
            lock.unlock();
        }
    }

    public void setReadIndex(int index){
        try{
            lock.lock();
            readIndex.set(index);
        }finally {
            lock.unlock();
        }
    }

    public synchronized void moveToNextReadIndex()throws IOException{
        try{
            lock.lock();
            int index = readIndex.incrementAndGet();
            if(index > writeIndex.get() ){
                readIndex.decrementAndGet();
                throw new IOException("Not found index : " + index);
            }
            setReadIndexOffset(0);
        }finally {
            lock.unlock();
        }
    }

    public synchronized void moveToNextWriteIndex() {
        try{
            lock.lock();
            writeIndex.incrementAndGet();
            setWriteIndexOffset(0);
        }finally {
            lock.unlock();
        }
    }

    public synchronized void moveToNextDataWriteIndex() {
        try{
            lock.lock();
            writeDataIndex.incrementAndGet();
            setWriteDataOffset(0);
        }finally {
            lock.unlock();
        }
    }

    public int getReadIndexOffset() {
        try{
            lock.lock();
            return readIndexOffset;
        }finally {
            lock.unlock();
        }
    }

    public void setReadIndexOffset(int readIndexOffset) {
        try{
            lock.lock();
        this.readIndexOffset = readIndexOffset;
        }finally {
            lock.unlock();
        }
    }

    public int getReadDataIndex() {
        try{
            lock.lock();
        return readDataIndex;
        }finally {
            lock.unlock();
        }
    }

    public void setReadDataIndex(int readStore) {
        try{
            lock.lock();
            this.readDataIndex = readStore;
        }finally {
            lock.unlock();
        }
    }

    public int getReadDataOffset() {
        try{
            lock.lock();
            return readDataOffset;
        }finally {
            lock.unlock();
        }
    }

    public void setReadDataOffset(int readDataOffset) {
        try{
            lock.lock();
            this.readDataOffset = readDataOffset;
        }finally {
            lock.unlock();
        }
    }

    public int getWriteIndex() {
        try{
            lock.lock();
            return writeIndex.get();
        }finally {
            lock.unlock();
        }
    }

    public void setWriteIndex0(int writeIndex) {
        try{
            lock.lock();
            this.writeIndex.set(writeIndex);
        }finally {
            lock.unlock();
        }
    }

    public int getWriteIndexOffset() {
        try{
            lock.lock();
            return writeIndexOffset;
        }finally {
            lock.unlock();
        }
    }

    public void setWriteIndexOffset(int writeIndexOffset) {
        try{
            lock.lock();
            this.writeIndexOffset = writeIndexOffset;
        }finally {
            lock.unlock();
        }
    }

    public int getWriteDataIndex() {
        try{
            lock.lock();
            return writeDataIndex.get();
        }finally {
            lock.unlock();
        }
    }

    public void setWriteDataIndex0(int writeDataIndex) {
        try{
            lock.lock();
            this.writeDataIndex.set(writeDataIndex);
        }finally {
            lock.unlock();
        }
    }

    public int getWriteDataOffset() {
        try{
            lock.lock();
            return writeDataOffset;
        }finally {
            lock.unlock();
        }
    }

    public void setWriteDataOffset(int writeDataOffset) {
        try{
            lock.lock();
            this.writeDataOffset = writeDataOffset;
        }finally {
            lock.unlock();
        }
    }

    public int getIndexFilePartitionLength() {
        return indexFilePartitionLength;
    }

    public int getDataFilePartitionLength() {
        return dataFilePartitionLength;
    }

    @Override
    public String toString() {
        return "MetaIO{" +
                "writeCount=" + getWriteCount() +
                ", readCount=" + getReadCount() +
                ", writeDataOffset=" + writeDataOffset +
                ", writeDataIndex=" + writeDataIndex +
                ", writeOffset=" + writeIndexOffset +
                ", writeIndex=" + writeIndex +
                ", readIndexOffset=" + readIndexOffset +
                ", readIndex=" + readIndex +
                ", readDataIndex=" + readDataIndex +
                ", readDataOffset=" + readDataOffset +
                '}';
    }
}
