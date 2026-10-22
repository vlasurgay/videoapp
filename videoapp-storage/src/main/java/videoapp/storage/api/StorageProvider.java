package videoapp.storage.api;


public interface StorageProvider {
        ObjectStorage uploads();
        ObjectStorage processed();
    }
