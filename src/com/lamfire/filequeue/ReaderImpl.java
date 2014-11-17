package com.lamfire.filequeue;


import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ReaderImpl implements Reader {
    private Lock lock = new ReentrantLock();
    private MetaBuffer meta;
    private IndexManager indexMgr;
    private DataManager dataMgr;

    private int _index;
    private int _offset;


    public ReaderImpl(MetaBuffer meta, IndexManager indexMgr, DataManager dataMgr)throws IOException{
        this.meta = meta;
        this.indexMgr = indexMgr;
        this.dataMgr = dataMgr;

        this._index = meta.getReadIndex();
        this._offset = meta.getReadIndexOffset();
    }

    @Override
    public boolean hashMore() {
        int wIndex = meta.getWriteIndex();
        int wOffset = meta.getWriteIndexOffset();
        if(wIndex >= _index && wOffset > _offset){
            return true;
        }
        return false;
    }

    private byte[] readData() throws IOException{
        try{
            lock.lock();
            IndexBuffer indexIO = indexMgr.getIndexBuffer(_index);
            indexIO.setReadOffset(_offset);

            Element element = indexIO.take();
            DataBuffer storeIO = dataMgr.getDataBuffer(element.getStore());
            storeIO.setReadOffset(element.getPosition());
            byte[] bytes = new byte[element.getLength()];
            storeIO.read(bytes);
            return bytes;
        }finally {
            lock.unlock();
        }
    }

    public void moveNext()throws IOException{
        if(!hashMore()){
            return ;
        }
        try{
            lock.lock();
            if((FileBuffer.MAX_FILE_LENGTH - _offset) < Element.ELEMENT_LENGTH){
                _index ++;
                _offset = 0;
            }
            _offset += Element.ELEMENT_LENGTH;
        }finally {
            lock.unlock();
        }
    }

    public byte[] read() throws IOException{
        if(!hashMore()){
            return null;
        }
        return readData();
    }

    public void moveTo(int i) throws IOException{
        try{
            lock.lock();
            int index = meta.getReadIndex();
            int indexOffset = meta.getReadIndexOffset();

            int skipOffset = i * Element.ELEMENT_LENGTH;
            int maxAvailableSpace = IndexBuffer.MAX_AVAILABLE_FILE_SPACE;
            int skipIdx = skipOffset / maxAvailableSpace;
            skipOffset = skipOffset % maxAvailableSpace ;
            index+= skipIdx;
            indexOffset += skipOffset;

            if(indexOffset > maxAvailableSpace) {
                index ++;
                indexOffset = indexOffset - maxAvailableSpace;
            }
            this._index = index;
            this._offset = indexOffset;
        }finally {
            lock.unlock();
        }
    }

    public int index() {
        return _index;
    }

    public int offset() {
        return _offset;
    }

    public void commit()throws IOException{
        meta.setReadIndex(index());
        meta.setReadIndexOffset(offset());
        meta.flush();
    }
}
