rm -rf packaged_build && mkdir -p packaged_build
echo $(env) > packaged_build/env.txt
echo ${BUILD_URL} > packaged_build/build_url.txt
