listView('3.1.Sys') {
    description('Jin Sys jobs')
    filterBuildQueue()
    filterExecutors()
    recurse()

    jobs {
        regex('(.*Sys.*)')
    }

    statusFilter(StatusFilter.ENABLED)
    columns {
        status()
        lastBuildConsole()
        name()
        testResult(1)
        lastSuccess()
        lastFailure()
        lastDuration()
        progressBar()
        buildButton()
        lastConfigurationModification()
    }
}

listView('3.2.Config') {
    description('Jenkins configuration jobs')
    filterBuildQueue()
    filterExecutors()
    recurse()

    jobs {
        regex('(.*[Cc]onfig.*)')
    }

    statusFilter(StatusFilter.ENABLED)
    columns {
        status()
        lastBuildConsole()
        name()
        testResult(1)
        lastSuccess()
        lastFailure()
        lastDuration()
        progressBar()
        buildButton()
        lastConfigurationModification()
    }
}

listView('All.Flat') {
    description('All jobs in flat mode')
    filterBuildQueue()
    filterExecutors()
    recurse()

    jobs {
        regex('.*')
    }

    statusFilter(StatusFilter.ENABLED)
    columns {
        status()
        lastBuildConsole()
        name()
        testResult(1)
        lastSuccess()
        lastFailure()
        lastDuration()
        progressBar()
        buildButton()
        lastConfigurationModification()
    }
}
