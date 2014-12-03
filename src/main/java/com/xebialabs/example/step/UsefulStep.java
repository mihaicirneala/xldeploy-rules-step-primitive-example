package com.xebialabs.example.step;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.flow.StepExitCode;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.rules.RulePostConstruct;
import com.xebialabs.deployit.plugin.api.rules.StepMetadata;
import com.xebialabs.deployit.plugin.api.rules.StepParameter;
import com.xebialabs.deployit.plugin.api.rules.StepPostConstructContext;
import com.xebialabs.deployit.plugin.api.services.Repository;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.tryFind;

@StepMetadata(name = "my-nifty-step")
public class UsefulStep implements Step {

    @StepParameter(name = "freemarkerContext", description = "Dictionary that contains all values available in the template", required = false, calculated = true)
    private Map<String, Object> vars = new HashMap<>();

    @Override
    public int getOrder() {
        return 50;
    }

    @Override
    public String getDescription() {
        return "This is a really nifty and useful step";
    }

    @Override
    public StepExitCode execute(ExecutionContext executionContext) throws Exception {
        executionContext.logOutput(String.format("Freemarker context: %s", vars));
        executionContext.logOutput(String.format("Default server url is: %s", defaultUrl));
        return StepExitCode.SUCCESS;
    }

    @StepParameter(name="defaultHostURL", description="The URL to contact first", required=true, calculated=true)
    private String defaultUrl;

    @RulePostConstruct
    private void lookupDefaultUrl(StepPostConstructContext ctx) {
        if (defaultUrl==null || defaultUrl.equals("")) {
            Repository repo = ctx.getRepository();
            Delta delta = ctx.getDelta();
            defaultUrl = findDefaultUrl(repo);
        }
    }

    private static String findDefaultUrl(Repository repo) {
        List<ConfigurationItem> search = repo.search(Type.valueOf("example.Server"));
        Optional<ConfigurationItem> configurationItemOptional = tryFind(search, new Predicate<ConfigurationItem>() {
            @Override
            public boolean apply(ConfigurationItem input) {
                return !Strings.isNullOrEmpty(input.getProperty("url").toString());
            }
        });

        return configurationItemOptional.orNull().getProperty("url");
    }
}
