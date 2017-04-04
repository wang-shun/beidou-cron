db.qtinfo_tmp.drop();
db.createCollection('qtinfo_tmp');
db.qtinfo_tmp.ensureIndex({"wordid":1,"regid":1});
