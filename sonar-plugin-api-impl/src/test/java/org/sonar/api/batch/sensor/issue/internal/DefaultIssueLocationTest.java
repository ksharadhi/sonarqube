/*
 * SonarQube
 * Copyright (C) 2009-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.api.batch.sensor.issue.internal;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.issue.MessageFormatting;
import org.sonar.api.batch.sensor.issue.NewMessageFormatting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

public class DefaultIssueLocationTest {

  private InputFile inputFile = new TestInputFileBuilder("foo", "src/Foo.php")
    .initMetadata("Foo\nBar\n")
    .build();

  @Test
  public void should_build() {
    assertThat(new DefaultIssueLocation()
      .on(inputFile)
      .message("pipo bimbo")
      .message()
    ).isEqualTo("pipo bimbo");
  }

  @Test
  public void not_allowed_to_call_on_twice() {
    assertThatThrownBy(() -> new DefaultIssueLocation()
      .on(inputFile)
      .on(inputFile)
      .message("Wrong way!"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("on() already called");
  }

  @Test
  public void prevent_too_long_messages() {
    assertThat(new DefaultIssueLocation()
      .on(inputFile)
      .message(StringUtils.repeat("a", 1333)).message()).hasSize(1333);

    assertThat(new DefaultIssueLocation()
      .on(inputFile)
      .message(StringUtils.repeat("a", 1334)).message()).hasSize(1333);
  }

  @Test
  public void should_ignore_messageFormatting_if_message_is_trimmed() {
    DefaultMessageFormatting messageFormatting = new DefaultMessageFormatting()
      .start(1500)
      .end(1501)
      .type(MessageFormatting.Type.CODE);

    DefaultIssueLocation location = new DefaultIssueLocation()
      .message(StringUtils.repeat("a", 2000), List.of(messageFormatting));

    assertThat(location.messageFormattings()).isEmpty();
  }

  @Test
  public void should_truncate_messageFormatting_if_necessary() {
    DefaultMessageFormatting messageFormatting = new DefaultMessageFormatting()
      .start(1300)
      .end(1501)
      .type(MessageFormatting.Type.CODE);

    DefaultIssueLocation location = new DefaultIssueLocation()
      .message(StringUtils.repeat("a", 2000), List.of(messageFormatting));

    assertThat(location.messageFormattings())
      .extracting(MessageFormatting::start, MessageFormatting::end)
      .containsOnly(tuple(1300, 1333));
  }

  @Test
  public void should_validate_message_formatting() {
    List<NewMessageFormatting> messageFormattings = List.of(new DefaultMessageFormatting()
      .start(1)
      .end(3)
      .type(MessageFormatting.Type.CODE));
    DefaultIssueLocation location = new DefaultIssueLocation();

    assertThatThrownBy(() -> location.message("aa", messageFormattings))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void prevent_null_character_in_message_text() {
    assertThatThrownBy(() -> new DefaultIssueLocation()
      .message("pipo " + '\u0000' + " bimbo"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Character \\u0000 is not supported in issue message");
  }

  @Test
  public void prevent_null_character_in_message_text_when_builder_has_been_initialized() {
    assertThatThrownBy(() -> new DefaultIssueLocation()
      .on(inputFile)
      .message("pipo " + '\u0000' + " bimbo"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageStartingWith("Character \\u0000 is not supported in issue message")
      .hasMessageEndingWith(", on component: src/Foo.php");
  }
}
