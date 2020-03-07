use std::collections::HashMap;
use std::fs::File;
use std::path::{Path, PathBuf};
use std::sync::{Arc, RwLock};

use serde::export::PhantomData;
use zip::result::ZipResult;
use zip::ZipArchive;

pub struct ZipFileSystem {
    source: RwLock<Option<ZipArchive<File>>>,
    modified_files: RwLock<HashMap<PathBuf, Arc<RwLock<ZipFileContent>>>>,
}

impl ZipFileSystem {
    pub fn new(path: impl AsRef<Path>) -> ZipResult<Self> {
        unimplemented!()
    }

    fn open(&self, path: impl AsRef<Path>) -> Result<ZipFile, ()> {
        unimplemented!()
    }

    fn flush(&mut self) {
        let result = self.source.write().unwrap().unwrap();
    }
}

impl Drop for ZipFileSystem {
    fn drop(&mut self) {
        self.flush();
    }
}

struct ZipFileContent {
    buffer: Vec<u8>,
}

struct ZipFileState {
    pos: usize,
    file_content: Arc<RwLock<ZipFileContent>>,
}

pub struct ZipFile<'a> {
    state: Arc<RwLock<ZipFileContent>>,
    marker: PhantomData<&'a ()>,
}