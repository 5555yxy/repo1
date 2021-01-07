package xyz.yxy.weather;

public class weather {
    int _id;
    int id;
    int pid;
    String city_code;
    String city_name;
    void set_id(int __id){
        id=__id;
    }
    void setPid(int _pid){
        pid=_pid;
    }
    void setCity_code(String _city_code){
        city_code=_city_code;
    }
    void setCity_name(String _city_name){
        city_name=_city_name;
    }
    void setIdid(int _idid){
        id=_idid;
    }
    void setRealid(int _realid){
        _id=_realid;
    }
    int getRealid(){
        return _id;
    }
    int getid(){
        return id;
    }
    int getPid(){
        return pid;
    }
    String getCity_code(){
        return city_code;
    }
    String getCity_name(){
        return city_name;
    }
}
