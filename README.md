Changes:

Errors throwing for debugging and Logs
Can it handle huge codebases correct throwing of exceptions / optimizations
Consolidating Actions

1: actions into a class instead of a list of map of map
2: testing jarTypeSolver by using customActions in its own jar input 
3: NodeMatcher: fix ordering, maybe flag, maybe error.
4: Order of json and multiple jsons to be used, sample.properties (config.json) will hold order of jsons, with relative path. output folder and jar file etc. will be handled internaly maybe in bin folder.
5: Validators handled in recipe per custom validator and also handled by point 4.