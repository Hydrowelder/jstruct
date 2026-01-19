# Generate Javadoc
mvn javadoc:javadoc

# Copy to /docs
mkdir -p docs
rm -rf docs/*
cp -r target/site/apidocs/* docs/
git add docs
git commit -m "Update Javadoc"
git push
