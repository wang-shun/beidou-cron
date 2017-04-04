db.ctinfo_tmp.drop();
db.createCollection('ctinfo_tmp');
db.ctinfo_tmp.ensureIndex({"wordid":1,"regid":1});
