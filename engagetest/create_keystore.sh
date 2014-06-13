export dname='cn=Demo, ou=Development, o=SilverPop, c=US'
export version='001'
export dir=`pwd`
export project=engagetest
export keystore_password=engagesdk
export key_alias=$project-$version
export appid=com.silverpop.engage.demo.engagetest
export keystore_file=$key_alias.keystore
export hash_file=$key_alias.hash

if [[ -z "$ANDROID_HOME" ]]; then
  export ANDROID_HOME=/opt/android-sdk
fi


keytool -genkey -v -keystore $dir/$keystore_file -alias $key_alias -keyalg RSA -keysize 2048 -validity 10000 -storepass $keystore_password -keypass $keystore_password  -dname "$dname"
keytool -storepass $keystore_password -keypass $keystore_password -exportcert -alias $key_alias -keystore $dir/$keystore_file | openssl sha1 -binary | openssl base64 > $hash_file

echo "created $hash_file"
cat $hash_file

echo "rebuild your apk with:"
echo "  keystore location: $dir/$keystore_file"
echo "  keystore password: $keystore_password"
echo "  alias: $key_alias"
