package com.github.ajalt.clikt.samples.repo

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

data class Repo(var home: String, val config: MutableMap<String, String>, var verbose: Boolean)

class Cli : CliktCommand(
        help = """Repo is a command line tool that showcases how to build complex
        command line interfaces with Clikt.

        This tool is supposed to look like a distributed version control
        system to show how something like this can be structured.""".trimIndent()) {
    val repoHome: String by option(help = "Changes the repository folder location.")
            .default(".repo")
    val config: List<Pair<String, String>> by option(help = "Overrides a config key/value pair.")
            .paired()
            .multiple()
    val verbose: Boolean by option("-v", "--verbose", help = "Enables verbose mode.")
            .flag()

    override fun run() {
        val repo = Repo(repoHome, HashMap(), verbose)
        for ((k, v) in config) {
            repo.config[k] = v
        }
        context.obj = repo
    }
}


class CloneCommand : CliktCommand(
        name = "clone",
        help = """Clones a repository.

        This will clone the repository at SRC into the folder DEST. If DEST
        is not provided this will automatically use the last path component
        of SRC and create that folder.""".trimIndent()) {
    val repo: Repo by requireObject()
    val src: String by argument()
    val dest: String? by argument().optional()
    val shallow: Boolean by option("--shallow/--deep",
            help = "Makes a checkout shallow or deep.  Deep by default.")
            .flag()

    val rev: String by option("--rev", "-r", help = "Clone a specific revision instead of HEAD.")
            .default("HEAD")

    override fun run() {
        val destName = dest ?: File(src).name
        println("Cloning repo $src to ${File(destName).absolutePath}")
        repo.home = destName
        if (shallow) {
            println("Making shallow checkout")
        }
        println("Checking out revision $rev")
    }
}

class DeleteCommand : CliktCommand(
        name = "delete",
        help = """Deletes a repository.

            This will throw away the current repository.""".trimIndent()) {
    val repo: Repo by requireObject()

    override fun run() {
        println("Destroying repo ${repo.home}")
        println("Deleted!")
    }
}

class SetUserCommand : CliktCommand(
        name = "setuser",
        help = """Sets the user credentials.

        This will override the current user config.""".trimIndent()) {
    val repo: Repo by requireObject()
    val username: String? by option(help = "The developer's shown username.")
    //            .prompt()  // TODO
    val email: String? by option(help = "The developer's email address.")
    val password: String? by option(help = "The login password.")
    //            .passwordPrompt() // TODO

    override fun run() {
        username?.let { repo.config["username"] = it }
        email?.let { repo.config["email"] = it }
        password?.let { repo.config["password"] = "*".repeat(it.length) }
        println("Changed credentials.")
    }
}


class CommitCommand : CliktCommand(
        name = "commit",
        help = """Commits outstanding changes.

        Commit changes to the given files into the repository.  You will need to
        "repo push" to push up your changes to other repositories.

        If a list of files is omitted, all changes reported by "repo status"
        will be committed.""".trimIndent()) {
    val repo: Repo by requireObject()
    val message: String? by option(help = "The commit message.")
    val files: List<File> by argument()
            .file()
            .multiple()

    override fun run() {
        val msg = if (message == null) {
            message // TODO: click.edit
        } else {
            message
        }
        println("Files to be commited: $files")
        println("Commit message:")
        println(msg)
    }
}

fun main(args: Array<String>) = Cli()
        .subcommands(CloneCommand(), DeleteCommand(), SetUserCommand(), CommitCommand())
        .main(args)

